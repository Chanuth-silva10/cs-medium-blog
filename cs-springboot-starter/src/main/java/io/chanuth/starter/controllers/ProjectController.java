package io.chanuth.starter.controllers;

import io.chanuth.starter.models.CreateProjectDto;
import io.chanuth.starter.models.ErrorResponse;
import io.chanuth.starter.models.Project;
import io.chanuth.starter.models.UpdateProjectDto;
import io.chanuth.starter.services.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/projects")
@Tag(name = "Projects", description = "Manage DevCraft projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a project", description = "Titles must be unique. Status defaults to PLANNING.")
    @ApiResponse(responseCode = "201", description = "The project was created")
    @ApiResponse(responseCode = "409", description = "A project with this title already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "The request body failed validation",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Project> createProject(@Valid @RequestBody CreateProjectDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(dto.toProject()));
    }

    @GetMapping
    @Operation(summary = "List all projects", description = "Newest first.")
    @ApiResponse(responseCode = "200", description = "All projects, newest first",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Project.class))))
    public ResponseEntity<List<Project>> findAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get one project")
    @ApiResponse(responseCode = "200", description = "The project")
    @ApiResponse(responseCode = "404", description = "No project has this id", content = @Content)
    public ResponseEntity<Project> findProject(
            @Parameter(description = "Project id", example = "1") @PathVariable Long id) {
        return projectService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Update a project", description = "Send only the fields you want to change.")
    @ApiResponse(responseCode = "200", description = "The updated project")
    @ApiResponse(responseCode = "404", description = "No project has this id", content = @Content)
    @ApiResponse(responseCode = "409", description = "Another project already uses this title",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "The request body failed validation",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "Project id", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateProjectDto dto) {
        return projectService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete a project")
    @ApiResponse(responseCode = "204", description = "The project was deleted", content = @Content)
    @ApiResponse(responseCode = "404", description = "No project has this id", content = @Content)
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project id", example = "1") @PathVariable Long id) {
        return projectService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
