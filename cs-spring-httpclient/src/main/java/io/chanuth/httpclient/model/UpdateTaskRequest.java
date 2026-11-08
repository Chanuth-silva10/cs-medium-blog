package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * A partial update. NON_NULL matters here: a field we leave out must not be sent as "null",
 * because the Task API would read that as "clear this value".
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateTaskRequest(
        @Size(max = 200) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        String category,
        Instant dueDate) {
}
