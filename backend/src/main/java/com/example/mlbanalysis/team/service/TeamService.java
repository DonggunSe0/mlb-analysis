package com.example.mlbanalysis.team.service;

import com.example.mlbanalysis.team.client.MlbTeamClient;
import com.example.mlbanalysis.team.client.dto.MlbNamedResource;
import com.example.mlbanalysis.team.client.dto.MlbStandingDivisionDto;
import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import com.example.mlbanalysis.team.client.dto.MlbTeamStandingDto;
import com.example.mlbanalysis.team.dto.TeamListResponse;
import com.example.mlbanalysis.team.dto.TeamResponse;
import com.example.mlbanalysis.team.dto.TeamStandingListResponse;
import com.example.mlbanalysis.team.dto.TeamStandingResponse;
import java.time.Year;
import java.util.Comparator;
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


    @Cacheable(value = "mlbStandings", key = "#season == null || #season.isBlank() ? T(java.time.Year).now().getValue().toString() : #season.trim()")
    public TeamStandingListResponse getStandings(String season) {
        String resolvedSeason = season == null || season.isBlank() ? String.valueOf(Year.now().getValue()) : season.trim();
        List<TeamStandingResponse> standings = mlbTeamClient.getStandings(resolvedSeason).stream()
                .flatMap(division -> division.teamRecords() == null
                        ? java.util.stream.Stream.<TeamStandingResponse>empty()
                        : division.teamRecords().stream().map(team -> toStandingResponse(resolvedSeason, division, team)))
                .sorted(Comparator
                        .comparing(TeamStandingResponse::leagueName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(TeamStandingResponse::divisionName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(TeamStandingResponse::divisionRank, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        return new TeamStandingListResponse(resolvedSeason, standings);
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


    private TeamStandingResponse toStandingResponse(String season, MlbStandingDivisionDto division, MlbTeamStandingDto team) {
        return new TeamStandingResponse(
                team.team() == null ? null : team.team().id(),
                team.team() == null ? null : team.team().name(),
                team.season() == null ? season : team.season(),
                leagueName(division.league()),
                divisionName(division.division()),
                parseRank(team.divisionRank()),
                parseRank(team.leagueRank()),
                team.gamesPlayed(),
                team.wins(),
                team.losses(),
                team.winningPercentage(),
                team.gamesBack(),
                team.wildCardGamesBack(),
                team.runDifferential(),
                team.divisionLeader()
        );
    }

    private Integer parseRank(String rank) {
        if (rank == null || rank.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(rank);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String leagueName(MlbNamedResource resource) {
        if (resource == null || resource.id() == null) {
            return null;
        }
        return switch (resource.id()) {
            case 103 -> "American League";
            case 104 -> "National League";
            default -> resource.name();
        };
    }

    private String divisionName(MlbNamedResource resource) {
        if (resource == null || resource.id() == null) {
            return null;
        }
        return switch (resource.id()) {
            case 200 -> "American League West";
            case 201 -> "American League East";
            case 202 -> "American League Central";
            case 203 -> "National League West";
            case 204 -> "National League East";
            case 205 -> "National League Central";
            default -> resource.name();
        };
    }

    private String nameOf(MlbNamedResource resource) {
        return resource == null ? null : resource.name();
    }
}
