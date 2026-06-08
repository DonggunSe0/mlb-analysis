package com.example.mlbanalysis.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserPreferenceRequest(
        @NotNull @Positive Long favoriteTeamId,
        @NotBlank @Size(max = 120) String favoriteTeamName
) {
}
