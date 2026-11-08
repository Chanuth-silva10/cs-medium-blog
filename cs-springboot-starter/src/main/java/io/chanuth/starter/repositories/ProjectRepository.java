package io.chanuth.starter.repositories;

import io.chanuth.starter.models.Project;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    List<Project> findAllByOrderByIdDesc();
}
