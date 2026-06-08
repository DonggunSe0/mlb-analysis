package com.example.mlbanalysis.game.dto;

public record GameResponse(
        Long gamePk,
        String gameDate,
        String status,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore
) {
}
