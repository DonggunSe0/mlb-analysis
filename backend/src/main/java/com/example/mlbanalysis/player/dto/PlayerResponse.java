package com.example.mlbanalysis.player.dto;

public record PlayerResponse(
        Integer id,
        String fullName,
        String birthCountry,
        Integer currentAge,
        String primaryPosition,
        String primaryPositionCode,
        String primaryPositionAbbreviation,
        Integer currentTeamId,
        String currentTeamName,
        String batSide,
        String pitchHand,
        String headshotUrl
) {
    public PlayerResponse(
            Integer id,
            String fullName,
            String birthCountry,
            Integer currentAge,
            String primaryPosition,
            String batSide,
            String pitchHand,
            String headshotUrl
    ) {
        this(id, fullName, birthCountry, currentAge, primaryPosition, null, null, null, null, batSide, pitchHand, headshotUrl);
    }
}
