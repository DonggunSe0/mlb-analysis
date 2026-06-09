package com.example.mlbanalysis.player.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.player.dto.PlayerBrowseResponse;
import com.example.mlbanalysis.player.dto.PlayerResponse;
import com.example.mlbanalysis.player.dto.PlayerSearchResponse;
import com.example.mlbanalysis.player.dto.PlayerTeamOptionResponse;
import com.example.mlbanalysis.player.dto.PlayerStatsResponse;
import java.util.List;
import com.example.mlbanalysis.player.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlayerController.class)
@Import(ApiExceptionHandler.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void getPlayerReturnsPublicResponse() throws Exception {
        when(playerService.getPlayer(545361)).thenReturn(new PlayerResponse(
                545361,
                "Mike Trout",
                "USA",
                34,
                "Outfielder",
                "Right",
                "Right",
                "https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/545361/headshot/67/current"
        ));

        mockMvc.perform(get("/api/v1/players/545361"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(545361))
                .andExpect(jsonPath("$.fullName").value("Mike Trout"))
                .andExpect(jsonPath("$.birthCountry").value("USA"))
                .andExpect(jsonPath("$.currentAge").value(34))
                .andExpect(jsonPath("$.primaryPosition").value("Outfielder"))
                .andExpect(jsonPath("$.batSide").value("Right"))
                .andExpect(jsonPath("$.pitchHand").value("Right"))
                .andExpect(jsonPath("$.headshotUrl").value("https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/545361/headshot/67/current"));
    }


    @Test
    void searchPlayersReturnsPublicEnvelope() throws Exception {
        when(playerService.searchPlayers("Mike Trout")).thenReturn(new PlayerSearchResponse("Mike Trout", List.of(
                new PlayerResponse(545361, "Mike Trout", "USA", 34, "Outfielder", "Right", "Right", "https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/545361/headshot/67/current")
        )));

        mockMvc.perform(get("/api/v1/players/search").param("name", "Mike Trout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mike Trout"))
                .andExpect(jsonPath("$.players.length()").value(1))
                .andExpect(jsonPath("$.players[0].id").value(545361))
                .andExpect(jsonPath("$.players[0].fullName").value("Mike Trout"))
                .andExpect(jsonPath("$.players[0].birthCountry").value("USA"))
                .andExpect(jsonPath("$.players[0].currentAge").value(34))
                .andExpect(jsonPath("$.players[0].primaryPosition").value("Outfielder"))
                .andExpect(jsonPath("$.players[0].batSide").value("Right"))
                .andExpect(jsonPath("$.players[0].pitchHand").value("Right"));
    }


    @Test
    void browsePlayersReturnsPagedEnvelope() throws Exception {
        when(playerService.browsePlayers("2026", "bo", "USA", 141, "SS", 0, 20)).thenReturn(new PlayerBrowseResponse(
                List.of(new PlayerResponse(666182, "Bo Bichette", "USA", 28, "Shortstop", "6", "SS", 141, "Toronto Blue Jays", "Right", "Right", "https://example.test/headshot.png")),
                0,
                20,
                1,
                1,
                true,
                true,
                List.of("USA"),
                List.of("Shortstop"),
                List.of(new PlayerTeamOptionResponse(141, "Toronto Blue Jays"))
        ));

        mockMvc.perform(get("/api/v1/players")
                        .param("season", "2026")
                        .param("q", "bo")
                        .param("country", "USA")
                        .param("teamId", "141")
                        .param("position", "SS")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players.length()").value(1))
                .andExpect(jsonPath("$.players[0].fullName").value("Bo Bichette"))
                .andExpect(jsonPath("$.players[0].currentTeamId").value(141))
                .andExpect(jsonPath("$.players[0].primaryPositionAbbreviation").value("SS"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.countries[0]").value("USA"))
                .andExpect(jsonPath("$.teams[0].name").value("Toronto Blue Jays"));
    }

    @Test
    void getPlayerStatsReturnsPublicResponse() throws Exception {
        when(playerService.getPlayerStats(545361, "2025", "hitting")).thenReturn(new PlayerStatsResponse(
                545361, "2025", "hitting", 130, 567, 456, 73, 106, 14, 1, 26, 64, 87, 6, 178,
                ".232", ".359", ".439", ".798", 2, 1, ".667", 200, 10, 9, 0, 4, 2450, ".301", 120, 150
        ));

        mockMvc.perform(get("/api/v1/players/545361/stats")
                        .param("season", "2025")
                        .param("group", "hitting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(545361))
                .andExpect(jsonPath("$.season").value("2025"))
                .andExpect(jsonPath("$.group").value("hitting"))
                .andExpect(jsonPath("$.gamesPlayed").value(130))
                .andExpect(jsonPath("$.homeRuns").value(26))
                .andExpect(jsonPath("$.ops").value(".798"));
    }

    @Test
    void getPlayerStatsMapsProviderFailureToBadGateway() throws Exception {
        when(playerService.getPlayerStats(545361, "2025", "hitting")).thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/players/545361/stats")
                        .param("season", "2025")
                        .param("group", "hitting"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB data is temporarily unavailable."));
    }


    @Test
    void browsePlayersMapsProviderFailureToBadGateway() throws Exception {
        when(playerService.browsePlayers(null, null, null, null, null, null, null)).thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB data is temporarily unavailable."));
    }

    @Test
    void searchPlayersMapsProviderFailureToBadGateway() throws Exception {
        when(playerService.searchPlayers("Mike Trout")).thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/players/search").param("name", "Mike Trout"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB data is temporarily unavailable."));
    }

    @Test
    void getPlayerMapsProviderFailureToBadGateway() throws Exception {
        when(playerService.getPlayer(545361)).thenThrow(new MlbApiException("provider unavailable"));

        mockMvc.perform(get("/api/v1/players/545361"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("MLB_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("MLB data is temporarily unavailable."));
    }
}
