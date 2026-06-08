package com.example.mlbanalysis.teamplayer.service;

import com.example.mlbanalysis.teamplayer.client.MlbTeamPlayerClient;
import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterEntryDto;
import com.example.mlbanalysis.teamplayer.dto.TeamPlayerListResponse;
import com.example.mlbanalysis.teamplayer.dto.TeamPlayerResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TeamPlayerService {

    private final MlbTeamPlayerClient mlbTeamPlayerClient;

    public TeamPlayerService(MlbTeamPlayerClient mlbTeamPlayerClient) {
        this.mlbTeamPlayerClient = mlbTeamPlayerClient;
    }

    @Cacheable(value = "mlbTeamPlayers", key = "#teamId")
    public TeamPlayerListResponse getTeamPlayers(Integer teamId) {
        return new TeamPlayerListResponse(teamId, mlbTeamPlayerClient.getTeamPlayers(teamId).stream()
                .map(this::toTeamPlayerResponse)
                .toList());
    }

    private TeamPlayerResponse toTeamPlayerResponse(MlbRosterEntryDto entry) {
        return new TeamPlayerResponse(
                entry.person() == null ? null : entry.person().id(),
                entry.person() == null ? null : entry.person().fullName(),
                entry.jerseyNumber(),
                entry.position() == null ? null : entry.position().name()
        );
    }
}
