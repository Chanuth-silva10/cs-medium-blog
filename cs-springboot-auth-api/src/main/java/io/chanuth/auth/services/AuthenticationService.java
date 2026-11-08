package io.chanuth.auth.services;

import io.chanuth.auth.dtos.LoginRequest;
import io.chanuth.auth.dtos.RegisterUserRequest;
import io.chanuth.auth.entities.RoleEnum;
import io.chanuth.auth.entities.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /** Everyone who signs up is a plain member. Higher roles are given, never chosen. */
    public User register(RegisterUserRequest input) {
        return userService.createUser(input, RoleEnum.MEMBER);
    }

    /**
     * Checks the email and password. A wrong password and an unknown email both end in the same
     * BadCredentialsException, so an attacker cannot use login to find out which emails exist.
     */
    public User authenticate(LoginRequest input) {
        Authentication result = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.email(), input.password()));
        return (User) result.getPrincipal();
    }
}
