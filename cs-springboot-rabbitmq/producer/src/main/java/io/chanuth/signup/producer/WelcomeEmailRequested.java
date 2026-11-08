package io.chanuth.signup.producer;

/**
 * The message we put in the queue: everything the mail worker needs to send the welcome email.
 * The consumer application has its own copy of this record, the JSON is the contract between them.
 */
public record WelcomeEmailRequested(String fullName, String emailAddress, String confirmationCode) {
}
