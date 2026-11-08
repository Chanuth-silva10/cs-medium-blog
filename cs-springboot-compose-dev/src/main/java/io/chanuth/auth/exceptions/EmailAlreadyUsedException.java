package io.chanuth.auth.exceptions;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("An account with the email " + email + " already exists");
    }
}
