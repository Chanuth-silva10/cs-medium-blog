package io.chanuth.auth.bootstrap;

import io.chanuth.auth.dtos.RegisterUserRequest;
import io.chanuth.auth.entities.RoleEnum;
import io.chanuth.auth.repositories.UserRepository;
import io.chanuth.auth.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Nobody can sign up as a super admin, so the first one is created at startup. Its email and
 * password come from environment variables. If they are not set, nothing is created: a default
 * password baked into the code would be the first thing an attacker tries.
 *
 * ApplicationRunner runs exactly once, after the application has fully started.
 */
@Component
public class SuperAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final String email;
    private final String password;

    public SuperAdminSeeder(
            UserService userService,
            UserRepository userRepository,
            @Value("${app.super-admin.email:}") String email,
            @Value("${app.super-admin.password:}") String password) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.warn("SUPER_ADMIN_EMAIL or SUPER_ADMIN_PASSWORD is not set, so no super admin was created");
            return;
        }
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userService.createUser(new RegisterUserRequest("Super Admin", email, password), RoleEnum.SUPER_ADMIN);
        log.info("Created the super admin account {}", email);
    }
}
