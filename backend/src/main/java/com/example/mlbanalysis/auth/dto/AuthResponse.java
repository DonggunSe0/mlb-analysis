package com.example.mlbanalysis.auth.dto;

import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt, CurrentUserResponse user) {
}
