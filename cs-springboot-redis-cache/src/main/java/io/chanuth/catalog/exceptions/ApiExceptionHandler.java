package io.chanuth.catalog.exceptions;

import io.chanuth.catalog.dtos.ErrorResponse;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(e.getMessage()));
    }

    // BindException covers both invalid query parameters and invalid JSON bodies.
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(BindException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        (error) -> error.getField(),
                        (error) -> String.valueOf(error.getDefaultMessage()),
                        (first, second) -> first));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse("Validation failed", errors));
    }
}
