package com.example.mlbanalysis.player.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.player.dto.PlayerResponse;
import com.example.mlbanalysis.player.dto.PlayerSearchResponse;
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
                "Right"
        ));

        mockMvc.perform(get("/api/v1/players/545361"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(545361))
                .andExpect(jsonPath("$.fullName").value("Mike Trout"))
                .andExpect(jsonPath("$.birthCountry").value("USA"))
                .andExpect(jsonPath("$.currentAge").value(34))
                .andExpect(jsonPath("$.primaryPosition").value("Outfielder"))
                .andExpect(jsonPath("$.batSide").value("Right"))
                .andExpect(jsonPath("$.pitchHand").value("Right"));
    }


    @Test
    void searchPlayersReturnsPublicEnvelope() throws Exception {
        when(playerService.searchPlayers("Mike Trout")).thenReturn(new PlayerSearchResponse("Mike Trout", List.of(
                new PlayerResponse(545361, "Mike Trout", "USA", 34, "Outfielder", "Right", "Right")
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
