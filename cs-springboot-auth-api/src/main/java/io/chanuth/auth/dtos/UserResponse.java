package io.chanuth.auth.dtos;

import io.chanuth.auth.entities.RoleEnum;
import io.chanuth.auth.entities.User;
import java.time.Instant;

/**
 * What the API shows about a user. Returning the entity itself would also send the password hash
 * and Spring Security's internal fields, so we copy only what a client needs.
 */
public record UserResponse(Long id, String fullName, String email, RoleEnum role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
                user.getRole().getName(), user.getCreatedAt());
    }
}
