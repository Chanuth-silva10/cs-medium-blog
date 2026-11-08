package io.chanuth.receipts.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.chanuth.receipts.storage.StorageExceptions.EmptyFileException;
import io.chanuth.receipts.storage.StorageExceptions.ReceiptNotFoundException;
import io.chanuth.receipts.storage.StorageExceptions.StorageException;
import io.chanuth.receipts.storage.StorageExceptions.UnsupportedFileTypeException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MultipartProperties multipart;

    public ApiExceptionHandler(MultipartProperties multipart) {
        this.multipart = multipart;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(String message, Map<String, String> errors) {

        static ErrorResponse of(String message) {
            return new ErrorResponse(message, null);
        }
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> missingFile(MissingServletRequestPartException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("The part '" + e.getRequestPartName() + "' is required"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooLarge() {
        // Read the real limit from the configuration, so the message is always true.
        long limitMb = multipart.getMaxFileSize().toMegabytes();
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ErrorResponse.of("The file is larger than the " + limitMb + " MB limit"));
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponse> unsupported(UnsupportedFileTypeException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ErrorResponse> empty(EmptyFileException e) {
        return ResponseEntity.unprocessableContent().body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ReceiptNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> badId(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("Invalid value for " + e.getName()));
    }

    /** The text fields failed validation, or could not be converted (a date that is not a date). */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> invalidForm(BindException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        (error) -> error.getField(),
                        (error) -> error.isBindingFailure() ? "has an invalid format" : String.valueOf(error.getDefaultMessage()),
                        (first, second) -> first));
        return ResponseEntity.unprocessableContent().body(new ErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> storage(StorageException e) {
        log.error("Storage failure", e);
        return ResponseEntity.internalServerError().body(ErrorResponse.of("The receipt could not be stored"));
    }
}
