package com.example.mlbanalysis.player.client.dto;

public record MlbPlayerDto(
        Integer id,
        String fullName,
        String birthCountry,
        Integer currentAge,
        MlbPlayerPositionDto primaryPosition,
        MlbPlayerSideDto batSide,
        MlbPlayerSideDto pitchHand
) {
}
