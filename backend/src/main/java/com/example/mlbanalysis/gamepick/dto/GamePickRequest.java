package com.example.mlbanalysis.gamepick.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GamePickRequest(
        @NotBlank String gameDate,
        @NotNull @Positive Long pickedTeamId
) {
}
