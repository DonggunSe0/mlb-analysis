package com.example.mlbanalysis.team.controller;

import com.example.mlbanalysis.team.dto.TeamListResponse;
import com.example.mlbanalysis.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(summary = "Return MLB teams from the external MLB Stats API")
    @GetMapping
    public TeamListResponse getTeams() {
        return teamService.getTeams();
    }
}
