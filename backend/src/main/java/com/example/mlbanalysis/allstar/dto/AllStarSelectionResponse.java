package com.example.mlbanalysis.allstar.dto;

public record AllStarSelectionResponse(
        String positionKey,
        Integer playerId,
        String playerName,
        String teamName
) {
}
