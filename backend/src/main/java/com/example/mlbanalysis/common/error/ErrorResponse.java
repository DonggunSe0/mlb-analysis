package com.example.mlbanalysis.common.error;

import java.util.List;

public record ErrorResponse(String code, String message, List<String> details) {
    public ErrorResponse(String code, String message) {
        this(code, message, List.of());
    }
}
