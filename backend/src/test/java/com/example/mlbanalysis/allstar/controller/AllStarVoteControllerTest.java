package com.example.mlbanalysis.allstar.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.allstar.dto.AllStarResultCandidateResponse;
import com.example.mlbanalysis.allstar.dto.AllStarResultPositionResponse;
import com.example.mlbanalysis.allstar.dto.AllStarVoteResultsResponse;
import com.example.mlbanalysis.allstar.service.AllStarVoteService;
import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AllStarVoteController.class)
@Import(ApiExceptionHandler.class)
@ActiveProfiles("login")
class AllStarVoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AllStarVoteService allStarVoteService;

    @Test
    void publicResultsDoNotRequireAuthorizationHeader() throws Exception {
        when(allStarVoteService.publicResults()).thenReturn(new AllStarVoteResultsResponse(
                LocalDate.parse("2026-06-09"),
                2L,
                List.of(new AllStarResultPositionResponse(
                        "P",
                        2L,
                        List.of(new AllStarResultCandidateResponse(
                                660271,
                                "Shohei Ohtani",
                                "Los Angeles Dodgers",
                                2L,
                                100,
                                true
                        ))
                ))
        ));

        mockMvc.perform(get("/api/v1/all-star/votes/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteDate").value("2026-06-09"))
                .andExpect(jsonPath("$.totalBallots").value(2))
                .andExpect(jsonPath("$.positions[0].positionKey").value("P"))
                .andExpect(jsonPath("$.positions[0].candidates[0].playerName").value("Shohei Ohtani"))
                .andExpect(jsonPath("$.positions[0].candidates[0].leading").value(true));
    }
}
