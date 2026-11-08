package io.chanuth.auth.repositories;

import io.chanuth.auth.entities.Role;
import io.chanuth.auth.entities.RoleEnum;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findByName(RoleEnum name);
}
