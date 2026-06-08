package com.example.mlbanalysis.gamepick.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.gamepick.dto.GamePickRequest;
import com.example.mlbanalysis.gamepick.dto.GamePickResponse;
import com.example.mlbanalysis.gamepick.service.GamePickService;
import com.example.mlbanalysis.gamepick.service.GamePickSummaryService;
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

@WebMvcTest(GamePickController.class)
@Import(ApiExceptionHandler.class)
@ActiveProfiles("login")
class GamePickControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GamePickService gamePickService;

    @MockitoBean
    private GamePickSummaryService gamePickSummaryService;

    @Test
    void getPickReturnsAuthenticatedUsersPickForGame() throws Exception {
        when(gamePickService.pick(eq("Bearer token"), eq(778899L)))
                .thenReturn(new GamePickResponse(778899L, 111L, "Los Angeles Dodgers", Instant.parse("2026-06-09T12:00:00Z")));

        mockMvc.perform(get("/api/v1/games/778899/pick").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gamePk").value(778899))
                .andExpect(jsonPath("$.pickedTeamId").value(111))
                .andExpect(jsonPath("$.pickedTeamName").value("Los Angeles Dodgers"));
    }

    @Test
    void postPickSavesAuthenticatedUsersPickForGame() throws Exception {
        when(gamePickService.submit(eq("Bearer token"), eq(778899L), any(GamePickRequest.class)))
                .thenReturn(new GamePickResponse(778899L, 147L, "New York Yankees", Instant.parse("2026-06-09T12:00:00Z")));

        mockMvc.perform(post("/api/v1/games/778899/pick")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickedTeamId\":147,\"pickedTeamName\":\"New York Yankees\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pickedTeamId").value(147))
                .andExpect(jsonPath("$.pickedTeamName").value("New York Yankees"));
    }

    @Test
    void missingTokenReturnsUnauthorizedEnvelope() throws Exception {
        when(gamePickService.pick(eq(null), eq(778899L)))
                .thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_MISSING", "Login is required."));

        mockMvc.perform(get("/api/v1/games/778899/pick"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_TOKEN_MISSING"));
    }
}
