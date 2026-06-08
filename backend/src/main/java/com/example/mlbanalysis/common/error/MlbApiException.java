package com.example.mlbanalysis.common.error;

public class MlbApiException extends RuntimeException {

    public MlbApiException(String message) {
        super(message);
    }

    public MlbApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
