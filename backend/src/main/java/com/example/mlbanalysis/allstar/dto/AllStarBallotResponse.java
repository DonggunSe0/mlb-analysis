package com.example.mlbanalysis.allstar.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AllStarBallotResponse(
        Long id,
        LocalDate voteDate,
        Instant createdAt,
        List<AllStarSelectionResponse> selections
) {
}
