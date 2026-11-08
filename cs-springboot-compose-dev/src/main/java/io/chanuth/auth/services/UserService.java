package io.chanuth.auth.services;

import io.chanuth.auth.dtos.RegisterUserRequest;
import io.chanuth.auth.dtos.UserResponse;
import io.chanuth.auth.entities.Role;
import io.chanuth.auth.entities.RoleEnum;
import io.chanuth.auth.entities.User;
import io.chanuth.auth.exceptions.EmailAlreadyUsedException;
import io.chanuth.auth.repositories.RoleRepository;
import io.chanuth.auth.repositories.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> allUsers() {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserResponse::from).toList();
    }

    /** Creates a user with the given role. Used for sign-ups, new admins and the seeded super admin. */
    public User createUser(RegisterUserRequest input, RoleEnum roleName) {
        if (userRepository.existsByEmail(input.email())) {
            throw new EmailAlreadyUsedException(input.email());
        }
        // The roles come from a Flyway migration, so a missing one means the database is not set up.
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("The role " + roleName + " is missing from the database"));

        User user = new User()
                .setFullName(input.fullName())
                .setEmail(input.email())
                .setPassword(passwordEncoder.encode(input.password()))
                .setRole(role);

        return userRepository.save(user);
    }
}
