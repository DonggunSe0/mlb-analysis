package com.example.mlbanalysis.game.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.game.dto.GameListResponse;
import com.example.mlbanalysis.game.dto.GameResponse;
import com.example.mlbanalysis.game.service.GameService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
@Import(ApiExceptionHandler.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Test
    void getGamesReturnsPublicEnvelope() throws Exception {
        when(gameService.getGames(LocalDate.parse("2026-06-01"))).thenReturn(new GameListResponse(List.of(
                new GameResponse(
                        822974L,
                        "2026-06-01T22:40:00Z",
                        "Final",
                        "Tampa Bay Rays",
                        "Detroit Tigers",
                        9,
                        10
                )
        )));

        mockMvc.perform(get("/api/v1/games").param("date", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.games.length()").value(1))
                .andExpect(jsonPath("$.games[0].gamePk").value(822974))
                .andExpect(jsonPath("$.games[0].gameDate").value("2026-06-01T22:40:00Z"))
                .andExpect(jsonPath("$.games[0].status").value("Final"))
                .andExpect(jsonPath("$.games[0].homeTeam").value("Tampa Bay Rays"))
                .andExpect(jsonPath("$.games[0].awayTeam").value("Detroit Tigers"))
                .andExpect(jsonPath("$.games[0].homeScore").value(9))
                .andExpect(jsonPath("$.games[0].awayScore").value(10));
    }

    @Test
    void invalidDateMapsToBadRequestEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/games").param("date", "bad-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details[0]").value("date has an invalid value."));
    }

    @Test
    void getGamesMapsProviderFailureToBadGateway() throws Exception {
        when(gameService.getGames(LocalDate.parse("2026-06-01")))
                .thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/games").param("date", "2026-06-01"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB data is temporarily unavailable."));
    }
}
