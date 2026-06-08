package com.example.mlbanalysis.common.error;

import com.example.mlbanalysis.auth.service.AuthException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    static final String MLB_API_UNAVAILABLE_CODE = "MLB_API_UNAVAILABLE";
    static final String MLB_API_UNAVAILABLE_MESSAGE = "MLB data is temporarily unavailable.";
    static final String INVALID_REQUEST_CODE = "INVALID_REQUEST";
    static final String INVALID_REQUEST_MESSAGE = "Request validation failed.";

    @ExceptionHandler(MlbApiException.class)
    public ResponseEntity<ErrorResponse> handleMlbApiException(MlbApiException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(MLB_API_UNAVAILABLE_CODE, MLB_API_UNAVAILABLE_MESSAGE));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ErrorResponse(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .sorted()
                .toList();
        return badRequest(details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return badRequest(List.of(exception.getName() + " has an invalid value."));
    }

    private ResponseEntity<ErrorResponse> badRequest(List<String> details) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(INVALID_REQUEST_CODE, INVALID_REQUEST_MESSAGE, details));
    }

    private String formatFieldError(FieldError error) {
        String message = error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage();
        return error.getField() + " " + message;
    }
}
