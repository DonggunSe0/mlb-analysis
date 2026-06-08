package com.example.mlbanalysis.gamepick.dto;

import java.util.List;

public record GamePickSummaryResponse(
        Long gamePk,
        Long totalPicks,
        List<GamePickTeamSummaryResponse> teams
) {
}
