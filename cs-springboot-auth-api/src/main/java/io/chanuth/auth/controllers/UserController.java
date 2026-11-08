package io.chanuth.auth.controllers;

import io.chanuth.auth.dtos.UserResponse;
import io.chanuth.auth.entities.User;
import io.chanuth.auth.services.UserService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Any signed-in user. @AuthenticationPrincipal hands us the user the JWT filter loaded. */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return UserResponse.from(currentUser);
    }

    /** Note: no "/" here. @GetMapping("/") would only match "/users/", and plain "/users" would be a 404. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<UserResponse> allUsers() {
        return userService.allUsers();
    }
}
