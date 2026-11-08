package io.chanuth.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * The one shape every error answer uses, including the ones written by the security filters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, Map<String, String> errors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, null);
    }
}
