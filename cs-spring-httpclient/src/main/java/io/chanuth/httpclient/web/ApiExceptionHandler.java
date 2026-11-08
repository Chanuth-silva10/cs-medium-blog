package io.chanuth.httpclient.web;

import io.chanuth.httpclient.client.TaskApiException;
import io.chanuth.httpclient.model.ErrorResponse;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

/**
 * Decides what OUR callers see when the Task API misbehaves. The rule of thumb: errors that are
 * the caller's fault (404, 422, 403...) are passed on, and problems on our side of the fence
 * (bad credentials, a down or slow upstream) become 502, 503 or 504.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(TaskApiException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamError(TaskApiException e) {
        return switch (e.status()) {
            case 400, 403, 404, 409, 422 -> ResponseEntity.status(e.status()).body(ErrorResponse.of(e.getMessage()));
            default -> {
                // Includes 401 (our service account failed to log in) and any 5xx from upstream.
                log.error("Task API problem (status {}): {}", e.status(), e.getMessage());
                yield ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(ErrorResponse.of("The Task API could not complete the request"));
            }
        };
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleNetworkProblem(ResourceAccessException e) {
        log.error("Could not reach the Task API: {}", e.getMessage());
        if (isTimeout(e)) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(ErrorResponse.of("The Task API took too long to answer"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("The Task API is unreachable"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        (error) -> error.getField(),
                        (error) -> String.valueOf(error.getDefaultMessage()),
                        (first, second) -> first));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("Validation failed", errors));
    }

    private static boolean isTimeout(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof SocketTimeoutException || t instanceof HttpTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
