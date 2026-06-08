package com.example.mlbanalysis.gamepick.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.gamepick.dto.GamePickSummaryResponse;
import com.example.mlbanalysis.gamepick.dto.GamePickTeamSummaryResponse;
import com.example.mlbanalysis.gamepick.service.GamePickService;
import com.example.mlbanalysis.gamepick.service.GamePickSummaryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GamePickController.class)
@Import(ApiExceptionHandler.class)
@ActiveProfiles("login")
class GamePickSummaryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GamePickService gamePickService;

    @MockitoBean
    private GamePickSummaryService gamePickSummaryService;

    @Test
    void summaryReturnsFanConsensusForGame() throws Exception {
        when(gamePickSummaryService.summary(778899L)).thenReturn(new GamePickSummaryResponse(
                778899L,
                4L,
                List.of(
                        new GamePickTeamSummaryResponse(111L, "Los Angeles Dodgers", 3L, 75, true),
                        new GamePickTeamSummaryResponse(147L, "New York Yankees", 1L, 25, false)
                )
        ));

        mockMvc.perform(get("/api/v1/games/778899/pick-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gamePk").value(778899))
                .andExpect(jsonPath("$.totalPicks").value(4))
                .andExpect(jsonPath("$.teams[0].pickedTeamName").value("Los Angeles Dodgers"))
                .andExpect(jsonPath("$.teams[0].pickPercentage").value(75))
                .andExpect(jsonPath("$.teams[0].leading").value(true));
    }
}
