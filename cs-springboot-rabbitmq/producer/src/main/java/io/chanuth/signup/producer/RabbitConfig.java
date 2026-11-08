package io.chanuth.signup.producer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * What exists on the RabbitMQ server: an exchange, a queue bound to it, and a dead letter queue
 * for messages that cannot be processed. Spring declares all of it when the application connects,
 * so nobody has to click through the web interface. The consumer declares exactly the same things:
 * RabbitMQ refuses a second declaration of a queue with different settings.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "devcraft.signups";
    public static final String QUEUE = "welcome-email";
    public static final String ROUTING_KEY = "welcome-email";
    public static final String DEAD_LETTER_EXCHANGE = "devcraft.signups.dlx";
    public static final String DEAD_LETTER_QUEUE = "welcome-email.dlq";

    @Bean
    DirectExchange signupsExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue welcomeEmailQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding welcomeEmailBinding() {
        return BindingBuilder.bind(welcomeEmailQueue()).to(signupsExchange()).with(ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_QUEUE);
    }

    /** Messages travel as JSON instead of Java serialization, so any language can read them. */
    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
