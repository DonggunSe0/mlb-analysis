package com.example.mlbanalysis.player.dto;

public record PlayerStatsResponse(
        Integer playerId,
        String season,
        String group,
        Integer gamesPlayed,
        Integer atBats,
        Integer runs,
        Integer hits,
        Integer doubles,
        Integer triples,
        Integer homeRuns,
        Integer rbi,
        Integer baseOnBalls,
        Integer strikeOuts,
        String avg,
        String obp,
        String slg,
        String ops,
        Integer stolenBases
) {
}
