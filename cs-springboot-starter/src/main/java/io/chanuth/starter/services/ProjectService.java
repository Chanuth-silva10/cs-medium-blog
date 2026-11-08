package io.chanuth.starter.services;

import io.chanuth.starter.models.Project;
import io.chanuth.starter.models.UpdateProjectDto;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

    Project create(Project project);

    List<Project> findAll();

    Optional<Project> findById(Long id);

    Optional<Project> update(Long id, UpdateProjectDto dto);

    boolean delete(Long id);
}
