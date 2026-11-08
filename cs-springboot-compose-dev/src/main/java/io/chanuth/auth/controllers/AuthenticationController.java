package io.chanuth.auth.controllers;

import io.chanuth.auth.dtos.LoginRequest;
import io.chanuth.auth.dtos.LoginResponse;
import io.chanuth.auth.dtos.RegisterUserRequest;
import io.chanuth.auth.dtos.UserResponse;
import io.chanuth.auth.entities.User;
import io.chanuth.auth.services.AuthenticationService;
import io.chanuth.auth.services.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authenticationService.authenticate(request);
        return new LoginResponse(jwtService.generateToken(user), jwtService.expirationInSeconds());
    }
}
