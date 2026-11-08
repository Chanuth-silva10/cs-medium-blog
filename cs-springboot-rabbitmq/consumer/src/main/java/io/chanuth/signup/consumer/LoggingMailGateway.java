package io.chanuth.signup.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Stands in for a real mail server: it writes the email to the log. */
@Component
public class LoggingMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailGateway.class);

    private final MailProperties properties;

    public LoggingMailGateway(MailProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendWelcome(String fullName, String emailAddress, String confirmationCode) {
        if (properties.simulateOutage()) {
            throw new MailUnavailableException("The mail server is not reachable");
        }
        log.info("Email to {} <{}>: welcome, your confirmation code is {}", fullName, emailAddress, confirmationCode);
    }
}
