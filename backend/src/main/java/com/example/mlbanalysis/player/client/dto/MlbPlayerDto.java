package com.example.mlbanalysis.player.client.dto;

public record MlbPlayerDto(
        Integer id,
        String fullName,
        String birthCountry,
        Integer currentAge,
        Boolean active,
        MlbPlayerTeamDto currentTeam,
        MlbPlayerPositionDto primaryPosition,
        MlbPlayerSideDto batSide,
        MlbPlayerSideDto pitchHand
) {
    public MlbPlayerDto(
            Integer id,
            String fullName,
            String birthCountry,
            Integer currentAge,
            MlbPlayerPositionDto primaryPosition,
            MlbPlayerSideDto batSide,
            MlbPlayerSideDto pitchHand
    ) {
        this(id, fullName, birthCountry, currentAge, null, null, primaryPosition, batSide, pitchHand);
    }
}
