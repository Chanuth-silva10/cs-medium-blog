package com.chanuth.role_base_access_control.bootstrap;

import com.chanuth.role_base_access_control.dtos.RegisterUserDto;
import com.chanuth.role_base_access_control.entities.Role;
import com.chanuth.role_base_access_control.entities.RoleEnum;
import com.chanuth.role_base_access_control.entities.User;
import com.chanuth.role_base_access_control.repositories.RoleRepository;
import com.chanuth.role_base_access_control.repositories.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository  userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createOwnerAdministrator();
    }

    private void createOwnerAdministrator() {
        RegisterUserDto userDto = new RegisterUserDto();
        userDto.setFullName("Company Owner").setEmail("owner.chanuth@email.com").setPassword("123456");

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.OWNER);
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        var user = new User()
                .setFullName(userDto.getFullName())
                .setEmail(userDto.getEmail())
                .setPassword(passwordEncoder.encode(userDto.getPassword()))
                .setRole(optionalRole.get());

        userRepository.save(user);
    }
}
