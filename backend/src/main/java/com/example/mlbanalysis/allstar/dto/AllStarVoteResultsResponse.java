package com.example.mlbanalysis.allstar.dto;

import java.time.LocalDate;
import java.util.List;

public record AllStarVoteResultsResponse(
        LocalDate voteDate,
        long totalBallots,
        List<AllStarResultPositionResponse> positions
) {
}
