package com.example.mlbanalysis.gamepick.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GamePickRequest(
        @NotNull @Positive Long pickedTeamId,
        @NotBlank @Size(max = 120) String pickedTeamName
) {
}
