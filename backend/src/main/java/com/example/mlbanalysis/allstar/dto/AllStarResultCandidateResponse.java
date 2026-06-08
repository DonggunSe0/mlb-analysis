package com.example.mlbanalysis.allstar.dto;

public record AllStarResultCandidateResponse(
        Integer playerId,
        String playerName,
        String teamName,
        long voteCount,
        int votePercentage,
        boolean leading
) {
}
