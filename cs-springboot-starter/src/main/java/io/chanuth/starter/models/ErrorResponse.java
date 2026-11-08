package io.chanuth.starter.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * The body of every error answer the API sends, so clients (and the API docs) have one shape to learn.
 */
@Schema(description = "Error answer returned by the API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "Short, human readable explanation") String message,
        @Schema(description = "Field name to problem, present only for validation errors") Map<String, String> errors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, null);
    }
}
