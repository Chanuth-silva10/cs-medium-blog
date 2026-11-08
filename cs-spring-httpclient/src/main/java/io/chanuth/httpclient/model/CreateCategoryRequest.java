package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCategoryRequest(@NotBlank String name, String description, String color) {
}
