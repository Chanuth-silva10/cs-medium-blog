package io.chanuth.signup.consumer;

/** Sends one email. The real implementation (SMTP, Mailgun, SES...) is not the topic of this chapter. */
public interface MailGateway {

    /** @throws MailUnavailableException when the mail server cannot be reached right now */
    void sendWelcome(String fullName, String emailAddress, String confirmationCode);

    class MailUnavailableException extends RuntimeException {
        public MailUnavailableException(String message) {
            super(message);
        }
    }
}
