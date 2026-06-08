package com.example.mlbanalysis.player.client.dto;

public record MlbPlayerStatsSplitDto(
        String season,
        MlbPlayerSeasonStatDto stat
) {
}
