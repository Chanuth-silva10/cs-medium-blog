package io.chanuth.signup.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param simulateOutage demo switch: when true every send fails like an unreachable mail server,
 *                       so you can watch the retries and the dead letter queue work
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(boolean simulateOutage) {
}
