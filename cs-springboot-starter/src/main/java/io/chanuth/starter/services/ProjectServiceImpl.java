package io.chanuth.starter.services;

import io.chanuth.starter.models.Project;
import io.chanuth.starter.models.UpdateProjectDto;
import io.chanuth.starter.repositories.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project create(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public Optional<Project> update(Long id, UpdateProjectDto dto) {
        return projectRepository.findById(id).map((project) -> {
            if (dto.title() != null) {
                project.setTitle(dto.title());
            }
            if (dto.description() != null) {
                project.setDescription(dto.description());
            }
            if (dto.status() != null) {
                project.setStatus(dto.status());
            }
            if (dto.maxMembers() != null) {
                project.setMaxMembers(dto.maxMembers());
            }
            if (dto.isPublic() != null) {
                project.setPublic(dto.isPublic());
            }
            return projectRepository.save(project);
        });
    }

    @Override
    public boolean delete(Long id) {
        if (!projectRepository.existsById(id)) {
            return false;
        }
        projectRepository.deleteById(id);
        return true;
    }
}
