package com.chanuth.role_base_access_control.bootstrap;

import com.chanuth.role_base_access_control.entities.Role;
import com.chanuth.role_base_access_control.entities.RoleEnum;
import com.chanuth.role_base_access_control.repositories.RoleRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;


    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadRoles();
    }

    private void loadRoles() {
        RoleEnum[] roleNames = new RoleEnum[] { RoleEnum.USER, RoleEnum.ADMIN, RoleEnum.OWNER };
        Map<RoleEnum, String> roleDescriptionMap = Map.of(
                RoleEnum.USER, "can access the authenticated user details for Default user",
                RoleEnum.ADMIN, "can access authenticated user details and list all users for administrator",
                RoleEnum.OWNER, "has access to all endpoints, including creating an administrator for owner"
        );

        Arrays.stream(roleNames).forEach((roleName) -> {
            Optional<Role> optionalRole = roleRepository.findByName(roleName);

            optionalRole.ifPresentOrElse(System.out::println, () -> {
                Role roleToCreate = new Role();

                roleToCreate.setName(roleName)
                        .setDescription(roleDescriptionMap.get(roleName));

                roleRepository.save(roleToCreate);
            });
        });
    }
}
