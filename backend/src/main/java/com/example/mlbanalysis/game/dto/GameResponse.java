package com.example.mlbanalysis.game.dto;

public record GameResponse(
        Long gamePk,
        String gameDate,
        String status,
        Integer homeTeamId,
        String homeTeam,
        Integer awayTeamId,
        String awayTeam,
        Integer homeScore,
        Integer awayScore
) {
}
