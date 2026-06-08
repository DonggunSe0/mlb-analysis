package com.example.mlbanalysis.player.dto;

public record PlayerResponse(
        Integer id,
        String fullName,
        String birthCountry,
        Integer currentAge,
        String primaryPosition,
        String batSide,
        String pitchHand
) {
}
