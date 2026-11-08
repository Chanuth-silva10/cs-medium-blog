package io.chanuth.signup.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

/** Starts a real RabbitMQ in Docker, so the queue, the retries and the dead letter queue are the real thing. */
@SpringBootTest
@Testcontainers
@Import(WelcomeEmailListenerTest.RecordingConfig.class)
class WelcomeEmailListenerTest {

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4-management-alpine");

    /** A mail gateway that remembers what it was asked to send and can be told to fail. */
    static class RecordingMailGateway implements MailGateway {
        final List<String> sent = new CopyOnWriteArrayList<>();
        final AtomicInteger attempts = new AtomicInteger();
        volatile int failuresLeft;

        @Override
        public void sendWelcome(String fullName, String emailAddress, String confirmationCode) {
            attempts.incrementAndGet();
            if (failuresLeft > 0) {
                failuresLeft--;
                throw new MailUnavailableException("The mail server is not reachable");
            }
            sent.add(emailAddress + ":" + confirmationCode);
        }

        void reset() {
            sent.clear();
            attempts.set(0);
            failuresLeft = 0;
        }
    }

    @TestConfiguration
    static class RecordingConfig {
        @Bean
        @Primary
        RecordingMailGateway recordingMailGateway() {
            return new RecordingMailGateway();
        }
    }

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    RecordingMailGateway gateway;

    @BeforeEach
    void clean() {
        rabbitAdmin.purgeQueue(RabbitConfig.QUEUE, false);
        rabbitAdmin.purgeQueue(RabbitConfig.DEAD_LETTER_QUEUE, false);
        gateway.reset();
    }

    void sendJson(String json) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        Message message = MessageBuilder.withBody(json.getBytes()).andProperties(properties).build();
        rabbitTemplate.send(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, message);
    }

    static String welcome(String email) {
        return "{\"fullName\":\"Nadia Perera\",\"emailAddress\":\"" + email + "\",\"confirmationCode\":\"482913\"}";
    }

    Message deadLetter() {
        return rabbitTemplate.receive(RabbitConfig.DEAD_LETTER_QUEUE, 10_000);
    }

    @Test
    void aMessageIsDeliveredToTheMailGatewayOnce() {
        sendJson(welcome("nadia@example.com"));

        await().atMost(Duration.ofSeconds(10)).until(() -> !gateway.sent.isEmpty());
        assertThat(gateway.sent).containsExactly("nadia@example.com:482913");
        assertThat(rabbitTemplate.receive(RabbitConfig.DEAD_LETTER_QUEUE, 500)).isNull();
    }

    @Test
    void aTemporaryFailureIsRetriedAndTheEmailIsFinallySent() {
        gateway.failuresLeft = 2;
        sendJson(welcome("nadia@example.com"));

        await().atMost(Duration.ofSeconds(15)).until(() -> !gateway.sent.isEmpty());
        assertThat(gateway.attempts.get()).isEqualTo(3);
        assertThat(rabbitTemplate.receive(RabbitConfig.DEAD_LETTER_QUEUE, 500)).isNull();
    }

    @Test
    void aMessageThatKeepsFailingEndsInTheDeadLetterQueueAfterThreeAttempts() {
        gateway.failuresLeft = 100;
        sendJson(welcome("nadia@example.com"));

        Message dead = deadLetter();
        assertThat(dead).isNotNull();
        assertThat(new String(dead.getBody())).contains("nadia@example.com");
        assertThat(gateway.attempts.get()).isEqualTo(3);
        assertThat(gateway.sent).isEmpty();
    }

    @Test
    void anInvalidAddressNeverReachesTheMailGatewayAndEndsInTheDeadLetterQueue() {
        sendJson(welcome("nobody"));

        assertThat(deadLetter()).isNotNull();
        assertThat(gateway.attempts.get()).isZero();
    }

    @Test
    void aMessageThatIsNotJsonGoesToTheDeadLetterQueue() {
        sendJson("this is not json");

        Message dead = deadLetter();
        assertThat(dead).isNotNull();
        assertThat(new String(dead.getBody())).isEqualTo("this is not json");
        assertThat(gateway.attempts.get()).isZero();
    }
}

