package com.example.mlbanalysis.allstar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AllStarBallotRequest(
        @Valid @NotEmpty @Size(max = 10) List<AllStarSelectionRequest> selections
) {
}
