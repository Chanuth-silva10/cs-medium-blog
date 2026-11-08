package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A category as the Task API sends it. MongoDB calls the identifier "_id"; we read that name
 * but expose it as a plain "id" to our own callers.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryDto(
        @JsonAlias("_id") String id,
        String name,
        String description,
        String color) {
}
