package com.example.mlbanalysis.allstar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AllStarSelectionRequest(
        @NotBlank @Size(max = 32) String positionKey,
        @NotNull Integer playerId,
        @NotBlank @Size(max = 120) String playerName,
        @Size(max = 80) String teamName
) {
}
