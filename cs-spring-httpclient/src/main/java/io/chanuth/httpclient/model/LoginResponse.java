package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * What POST /auth/login answers: the token and how many seconds it stays valid.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(String token, long expiresIn) {
}
