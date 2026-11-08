package io.chanuth.clubs.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(String message, Map<String, String> errors) {
        static ErrorResponse of(String message) {
            return new ErrorResponse(message, null);
        }
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(e.getMessage()));
    }

    /** A unique index (club name or code) refused the write. */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("A club with this name or code already exists"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap((error) -> error.getField(),
                        (error) -> String.valueOf(error.getDefaultMessage()), (first, second) -> first));
        return ResponseEntity.unprocessableContent().body(new ErrorResponse("Validation failed", errors));
    }

    /** Malformed JSON, or a value that is not part of an enum or not a date. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable() {
        return ResponseEntity.badRequest().body(ErrorResponse.of("The request body is missing or not valid JSON"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> badParameter(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("Invalid value for " + e.getName()));
    }
}
