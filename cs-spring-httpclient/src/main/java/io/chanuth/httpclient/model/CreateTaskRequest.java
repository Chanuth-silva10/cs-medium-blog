package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * What a caller sends us to create a task. Fields left out are not sent upstream at all,
 * so the Task API applies its own defaults.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        @NotBlank String category,
        Instant dueDate) {
}
