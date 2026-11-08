package io.chanuth.signup.consumer;

/** The message as the producer sends it. Same JSON, our own class: the two applications share no code. */
public record WelcomeEmailRequested(String fullName, String emailAddress, String confirmationCode) {
}
