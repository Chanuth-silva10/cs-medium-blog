package io.chanuth.auth.exceptions;

import io.chanuth.auth.dtos.ErrorResponse;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Errors raised inside controllers and services. Problems with the token itself, and "you may not
 * do this" from @PreAuthorize, are answered by the security handlers instead.
 *
 * There is deliberately no catch-all @ExceptionHandler(Exception.class): it would also swallow
 * Spring Security's access-denied exception and turn a correct 403 into a 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.of("Email or password is incorrect"));
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ErrorResponse> handleAccountStatus() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.of("This account is locked or disabled"));
    }

    // The second handler covers two sign-ups with the same email arriving at the same moment:
    // both pass the existsByEmail check, and the database's unique rule stops the second one.
    @ExceptionHandler({EmailAlreadyUsedException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDuplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of("An account with this email already exists"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        (error) -> error.getField(),
                        (error) -> String.valueOf(error.getDefaultMessage()),
                        (first, second) -> first));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse("Validation failed", errors));
    }
}
