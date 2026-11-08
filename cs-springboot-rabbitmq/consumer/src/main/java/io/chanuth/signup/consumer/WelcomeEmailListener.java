package io.chanuth.signup.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WelcomeEmailListener {

    private static final Logger log = LoggerFactory.getLogger(WelcomeEmailListener.class);

    private final MailGateway mailGateway;

    public WelcomeEmailListener(MailGateway mailGateway) {
        this.mailGateway = mailGateway;
    }

    /**
     * Called for every message of the queue. If the method returns, the message is acknowledged
     * and removed. If it throws, the retry rules in application.properties apply, and a message
     * that still fails goes to the dead letter queue.
     */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(WelcomeEmailRequested message) {
        if (message.emailAddress() == null || !message.emailAddress().contains("@")) {
            // A bad address will not get better. After the retries the message ends in the dead letter queue.
            throw new AmqpRejectAndDontRequeueException("Invalid email address in the message");
        }
        log.info("Message received for {}", message.fullName());
        mailGateway.sendWelcome(message.fullName(), message.emailAddress(), message.confirmationCode());
    }
}

