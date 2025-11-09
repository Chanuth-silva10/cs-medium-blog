package com.chanuth.consumer.dtos;

public record UserRegisteredPayload (String fullName, String emailAddress, int confirmationCode) {

}
