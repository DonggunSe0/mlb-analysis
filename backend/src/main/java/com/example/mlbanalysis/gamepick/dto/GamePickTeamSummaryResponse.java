package com.example.mlbanalysis.gamepick.dto;

public record GamePickTeamSummaryResponse(
        Long pickedTeamId,
        String pickedTeamName,
        Long pickCount,
        int pickPercentage,
        boolean leading
) {
}
