package io.chanuth.signup.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Starts a real RabbitMQ in Docker and reads back what the producer really published. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SignupPublishTest {

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4-management-alpine");

    @Autowired
    MockMvc mvc;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    JsonMapper json;

    @BeforeEach
    void clean() {
        rabbitAdmin.purgeQueue(RabbitConfig.QUEUE, false);
    }

    @Test
    void aSignupPutsAPersistentJsonMessageOnTheQueue() throws Exception {
        String response = mvc.perform(post("/signups").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nadia Perera\",\"email\":\"nadia@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").exists())
                .andReturn().getResponse().getContentAsString();
        String requestId = json.readTree(response).get("requestId").asString();

        Message message = rabbitTemplate.receive(RabbitConfig.QUEUE, 5_000);
        assertThat(message).isNotNull();
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(message.getMessageProperties().getReceivedDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(message.getMessageProperties().getMessageId()).isEqualTo(requestId);
        JsonNode body = json.readTree(new String(message.getBody()));
        assertThat(body.get("fullName").asString()).isEqualTo("Nadia Perera");
        assertThat(body.get("emailAddress").asString()).isEqualTo("nadia@example.com");
        assertThat(body.get("confirmationCode").asString()).matches("\\d{6}");
    }

    @Test
    void everySignupGetsItsOwnCodeAndMessageId() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/signups").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Nadia\",\"email\":\"nadia@example.com\"}")).andExpect(status().isAccepted());
        }
        Message first = rabbitTemplate.receive(RabbitConfig.QUEUE, 5_000);
        Message second = rabbitTemplate.receive(RabbitConfig.QUEUE, 5_000);
        assertThat(first.getMessageProperties().getMessageId()).isNotEqualTo(second.getMessageProperties().getMessageId());
    }

    @Test
    void anInvalidSignupIsRefusedAndNothingIsQueued() throws Exception {
        mvc.perform(post("/signups").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
        mvc.perform(post("/signups").contentType(MediaType.APPLICATION_JSON).content("{\"name\":"))
                .andExpect(status().isBadRequest());

        assertThat(rabbitTemplate.receive(RabbitConfig.QUEUE, 500)).isNull();
    }
}

