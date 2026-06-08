package com.example.mlbanalysis.teamplayer.controller;

import com.example.mlbanalysis.teamplayer.dto.TeamPlayerListResponse;
import com.example.mlbanalysis.teamplayer.service.TeamPlayerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/players")
public class TeamPlayerController {

    private final TeamPlayerService teamPlayerService;

    public TeamPlayerController(TeamPlayerService teamPlayerService) {
        this.teamPlayerService = teamPlayerService;
    }

    @Operation(summary = "Return active MLB players for a team from the external MLB Stats API")
    @GetMapping
    public TeamPlayerListResponse getTeamPlayers(@PathVariable Integer teamId) {
        return teamPlayerService.getTeamPlayers(teamId);
    }
}
