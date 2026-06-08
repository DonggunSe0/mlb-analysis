package com.example.mlbanalysis.team.service;

import com.example.mlbanalysis.team.client.MlbTeamClient;
import com.example.mlbanalysis.team.client.dto.MlbNamedResource;
import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import com.example.mlbanalysis.team.dto.TeamListResponse;
import com.example.mlbanalysis.team.dto.TeamResponse;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    private final MlbTeamClient mlbTeamClient;

    public TeamService(MlbTeamClient mlbTeamClient) {
        this.mlbTeamClient = mlbTeamClient;
    }

    @Cacheable("mlbTeams")
    public TeamListResponse getTeams() {
        List<TeamResponse> teams = mlbTeamClient.getTeams().stream()
                .map(this::toTeamResponse)
                .toList();
        return new TeamListResponse(teams);
    }

    private TeamResponse toTeamResponse(MlbTeamDto team) {
        return new TeamResponse(
                team.id(),
                team.name(),
                team.abbreviation(),
                team.teamName(),
                team.locationName(),
                nameOf(team.league()),
                nameOf(team.division()),
                nameOf(team.venue()),
                team.active()
        );
    }

    private String nameOf(MlbNamedResource resource) {
        return resource == null ? null : resource.name();
    }
}
