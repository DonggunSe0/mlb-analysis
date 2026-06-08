package com.example.mlbanalysis.teamplayer.dto;

public record TeamPlayerResponse(
        Integer playerId,
        String fullName,
        String jerseyNumber,
        String position
) {
}
