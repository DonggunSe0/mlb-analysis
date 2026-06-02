package com.example.mlbanalysis.team.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.team.dto.TeamListResponse;
import com.example.mlbanalysis.team.dto.TeamResponse;
import com.example.mlbanalysis.team.service.TeamService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeamController.class)
@Import(ApiExceptionHandler.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    void getTeamsReturnsPublicEnvelope() throws Exception {
        when(teamService.getTeams()).thenReturn(new TeamListResponse(List.of(new TeamResponse(
                133,
                "Athletics",
                "ATH",
                "Athletics",
                "Sacramento",
                "American League",
                "American League West",
                "Sutter Health Park",
                true
        ))));

        mockMvc.perform(get("/api/v1/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teams[0].id").value(133))
                .andExpect(jsonPath("$.teams[0].name").value("Athletics"))
                .andExpect(jsonPath("$.teams[0].abbreviation").value("ATH"))
                .andExpect(jsonPath("$.teams[0].teamName").value("Athletics"))
                .andExpect(jsonPath("$.teams[0].locationName").value("Sacramento"))
                .andExpect(jsonPath("$.teams[0].leagueName").value("American League"))
                .andExpect(jsonPath("$.teams[0].divisionName").value("American League West"))
                .andExpect(jsonPath("$.teams[0].venueName").value("Sutter Health Park"))
                .andExpect(jsonPath("$.teams[0].active").value(true));
    }

    @Test
    void getTeamsMapsProviderFailureToBadGateway() throws Exception {
        when(teamService.getTeams()).thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/teams"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB team data is temporarily unavailable."));
    }
}
