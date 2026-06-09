package com.example.mlbanalysis.allstar.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicAllStarVoteController.class)
@Import(ApiExceptionHandler.class)
@ActiveProfiles("phase1")
class PublicAllStarVoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicResultsReturnEmptyPhaseOneStateWithoutLoginProfile() throws Exception {
        mockMvc.perform(get("/api/v1/all-star/votes/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBallots").value(0))
                .andExpect(jsonPath("$.positions").isArray())
                .andExpect(jsonPath("$.positions").isEmpty());
    }
}
