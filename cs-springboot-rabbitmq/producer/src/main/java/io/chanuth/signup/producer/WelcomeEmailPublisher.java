package io.chanuth.signup.producer;

import java.security.SecureRandom;
import java.util.UUID;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class WelcomeEmailPublisher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RabbitTemplate rabbitTemplate;

    public WelcomeEmailPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Puts the request on the queue and returns the id of the message. */
    public String publish(String fullName, String emailAddress) {
        String messageId = UUID.randomUUID().toString();
        // A confirmation code is a secret: SecureRandom, not java.util.Random.
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        WelcomeEmailRequested event = new WelcomeEmailRequested(fullName, emailAddress, code);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event, (message) -> {
            MessageProperties properties = message.getMessageProperties();
            properties.setMessageId(messageId);
            return message;
        });
        return messageId;
    }
}
