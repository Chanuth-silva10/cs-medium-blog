package io.chanuth.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * BCrypt only looks at the first 72 bytes of a password, and Spring Security refuses longer ones,
 * so the limit is checked here and the caller gets a clear 422 instead of a server error.
 */
public record RegisterUserRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
