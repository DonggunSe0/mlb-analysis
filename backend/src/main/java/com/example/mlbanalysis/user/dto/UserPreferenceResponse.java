package com.example.mlbanalysis.user.dto;

public record UserPreferenceResponse(
        Long favoriteTeamId,
        String favoriteTeamName
) {
}
