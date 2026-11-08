package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * A task as the Task API returns it. Fields we do not care about are ignored, so the upstream API
 * can add new ones without breaking us.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskDto(
        @JsonAlias("_id") String id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        CategoryRef category,
        Instant dueDate,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt) {
}
