package com.chanuth.role_base_access_control.repositories;

import com.chanuth.role_base_access_control.entities.Role;
import com.chanuth.role_base_access_control.entities.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
