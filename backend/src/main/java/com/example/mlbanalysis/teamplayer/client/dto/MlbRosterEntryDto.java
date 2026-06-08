package com.example.mlbanalysis.teamplayer.client.dto;

public record MlbRosterEntryDto(
        MlbRosterPersonDto person,
        String jerseyNumber,
        MlbRosterPositionDto position
) {
}
