package io.chanuth.auth.controllers;

import io.chanuth.auth.dtos.RegisterUserRequest;
import io.chanuth.auth.dtos.UserResponse;
import io.chanuth.auth.entities.RoleEnum;
import io.chanuth.auth.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admins")
@RestController
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createAdministrator(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse admin = UserResponse.from(userService.createUser(request, RoleEnum.ADMIN));
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }
}
