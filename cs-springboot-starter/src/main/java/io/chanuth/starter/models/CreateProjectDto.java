package io.chanuth.starter.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

// Same Lombok quirk as on Project: ignore the implicit "public" name so only "isPublic" exists.
@JsonIgnoreProperties({"public"})
@Schema(description = "Everything needed to create a project")
@Data
@NoArgsConstructor
public class CreateProjectDto {

    @Schema(example = "DevCraft API", description = "Unique project title")
    @NotBlank
    @Size(max = 120)
    private String title;

    @Schema(example = "The core backend for the DevCraft series")
    private String description;

    @Schema(description = "Defaults to PLANNING when left out")
    private ProjectStatusEnum status = ProjectStatusEnum.PLANNING;

    @Schema(example = "5")
    @PositiveOrZero
    private int maxMembers;

    @Schema(example = "true")
    @JsonProperty("isPublic")
    private boolean isPublic;

    public Project toProject() {
        return new Project()
                .setTitle(title)
                .setDescription(description)
                .setStatus(status)
                .setMaxMembers(maxMembers)
                .setPublic(isPublic);
    }
}
