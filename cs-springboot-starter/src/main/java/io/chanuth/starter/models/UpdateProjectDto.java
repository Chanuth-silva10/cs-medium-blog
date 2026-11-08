package io.chanuth.starter.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Partial update payload. Any null field is left unchanged.
 */
@Schema(description = "Fields to change. Anything you leave out stays as it is.")
public record UpdateProjectDto(
        @Schema(example = "DevCraft API v2")
        @Size(max = 120)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String title,
        String description,
        ProjectStatusEnum status,
        @PositiveOrZero Integer maxMembers,
        @JsonProperty("isPublic") Boolean isPublic) {
}
