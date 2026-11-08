package io.chanuth.auth.dtos;

/**
 * The token and its lifetime in seconds, the same unit OAuth 2 uses for "expires_in".
 */
public record LoginResponse(String token, long expiresIn) {
}
