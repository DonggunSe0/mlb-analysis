package com.example.mlbanalysis.common.error;

import com.example.mlbanalysis.auth.service.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    static final String MLB_API_UNAVAILABLE_CODE = "MLB_API_UNAVAILABLE";
    static final String MLB_API_UNAVAILABLE_MESSAGE = "MLB data is temporarily unavailable.";

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
}
