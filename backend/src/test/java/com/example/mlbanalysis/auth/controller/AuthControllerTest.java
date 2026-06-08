package com.example.mlbanalysis.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.auth.dto.AuthResponse;
import com.example.mlbanalysis.auth.dto.CurrentUserResponse;
import com.example.mlbanalysis.auth.dto.RegisterRequest;
import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(ApiExceptionHandler.class)
@ActiveProfiles("login")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerReturnsCreatedAuthEnvelope() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(new AuthResponse(
                "token-123",
                Instant.parse("2026-06-16T00:00:00Z"),
                new CurrentUserResponse(1L, "fan@example.com", "Fan")
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"fan@example.com\",\"displayName\":\"Fan\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.expiresAt").value("2026-06-16T00:00:00Z"))
                .andExpect(jsonPath("$.user.email").value("fan@example.com"))
                .andExpect(jsonPath("$.user.displayName").value("Fan"));
    }

    @Test
    void meReturnsCurrentUserFromBearerToken() throws Exception {
        when(authService.currentUser(eq("Bearer token-123")))
                .thenReturn(new CurrentUserResponse(1L, "fan@example.com", "Fan"));

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fan@example.com"));
    }

    @Test
    void authExceptionMapsToPublicErrorEnvelope() throws Exception {
        when(authService.currentUser(any()))
                .thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_MISSING", "Login is required."));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath("$.message").value("Login is required."));
    }
}
