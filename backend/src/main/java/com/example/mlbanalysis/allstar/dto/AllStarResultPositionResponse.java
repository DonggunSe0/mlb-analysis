package com.example.mlbanalysis.allstar.dto;

import java.util.List;

public record AllStarResultPositionResponse(
        String positionKey,
        long totalVotes,
        List<AllStarResultCandidateResponse> candidates
) {
}
