package com.example.mlbanalysis.player.client.dto;

public record MlbPlayerSeasonStatDto(
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
