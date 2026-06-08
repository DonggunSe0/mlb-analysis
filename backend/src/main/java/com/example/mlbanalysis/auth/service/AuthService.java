package com.example.mlbanalysis.auth.service;

import com.example.mlbanalysis.auth.config.AuthProperties;
import com.example.mlbanalysis.auth.dto.AuthRequest;
import com.example.mlbanalysis.auth.dto.AuthResponse;
import com.example.mlbanalysis.auth.dto.CurrentUserResponse;
import com.example.mlbanalysis.auth.dto.RegisterRequest;
import com.example.mlbanalysis.auth.entity.AuthToken;
import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.repository.AuthTokenRepository;
import com.example.mlbanalysis.auth.repository.AuthUserRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class AuthService {
    private final AuthUserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;
    private final AuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthUserRepository userRepository, AuthTokenRepository tokenRepository, PasswordHasher passwordHasher, Clock clock, AuthProperties properties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
        this.properties = properties;
    }

    AuthService(AuthUserRepository userRepository, AuthTokenRepository tokenRepository, PasswordHasher passwordHasher, Clock clock) {
        this(userRepository, tokenRepository, passwordHasher, clock, new AuthProperties());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "Email is already registered.");
        }
        AuthUser user = userRepository.save(new AuthUser(email, request.displayName().trim(), passwordHasher.hash(request.password())));
        return issueToken(user);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        AuthUser user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(candidate -> passwordHasher.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password."));
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(String authorizationHeader) {
        AuthToken authToken = findValidToken(extractBearerToken(authorizationHeader));
        return toCurrentUser(authToken.getUser());
    }

    @Transactional
    public void logout(String authorizationHeader) {
        tokenRepository.deleteByToken(extractBearerToken(authorizationHeader));
    }

    private AuthResponse issueToken(AuthUser user) {
        tokenRepository.deleteByExpiresAtBefore(clock.instant());
        Instant expiresAt = clock.instant().plus(properties.getTokenTtl());
        AuthToken token = tokenRepository.save(new AuthToken(generateToken(), user, expiresAt));
        return new AuthResponse(token.getToken(), expiresAt, toCurrentUser(user));
    }

    private AuthToken findValidToken(String token) {
        AuthToken authToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID", "Login is required."));
        if (authToken.getExpiresAt().isBefore(clock.instant())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "Login has expired.");
        }
        return authToken;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_MISSING", "Login is required.");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_MISSING", "Login is required.");
        }
        return token;
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(); }
    private CurrentUserResponse toCurrentUser(AuthUser user) { return new CurrentUserResponse(user.getId(), user.getEmail(), user.getDisplayName()); }

    private String generateToken() {
        byte[] bytes = new byte[properties.getTokenBytes()];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
