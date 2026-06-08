package com.example.mlbanalysis.game.client.dto;

public record MlbGameDto(
        Long gamePk,
        String gameDate,
        MlbGameStatusDto status,
        MlbGameTeamsDto teams
) {
}
