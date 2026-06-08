package com.example.mlbanalysis.teamplayer.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.teamplayer.dto.TeamPlayerListResponse;
import com.example.mlbanalysis.teamplayer.dto.TeamPlayerResponse;
import com.example.mlbanalysis.teamplayer.service.TeamPlayerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeamPlayerController.class)
@Import(ApiExceptionHandler.class)
class TeamPlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamPlayerService teamPlayerService;

    @Test
    void getTeamPlayersReturnsPublicEnvelope() throws Exception {
        when(teamPlayerService.getTeamPlayers(133)).thenReturn(new TeamPlayerListResponse(133, List.of(
                new TeamPlayerResponse(675961, "Alika Williams", "12", "Second Base")
        )));

        mockMvc.perform(get("/api/v1/teams/133/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(133))
                .andExpect(jsonPath("$.players.length()").value(1))
                .andExpect(jsonPath("$.players[0].playerId").value(675961))
                .andExpect(jsonPath("$.players[0].fullName").value("Alika Williams"))
                .andExpect(jsonPath("$.players[0].jerseyNumber").value("12"))
                .andExpect(jsonPath("$.players[0].position").value("Second Base"));
    }

    @Test
    void getTeamPlayersMapsProviderFailureToBadGateway() throws Exception {
        when(teamPlayerService.getTeamPlayers(133)).thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/teams/133/players"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB data is temporarily unavailable."));
    }
}
