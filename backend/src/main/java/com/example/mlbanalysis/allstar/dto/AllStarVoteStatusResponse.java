package com.example.mlbanalysis.allstar.dto;

import java.time.LocalDate;

public record AllStarVoteStatusResponse(
        boolean canVote,
        LocalDate voteDate,
        AllStarBallotResponse ballot
) {
}
