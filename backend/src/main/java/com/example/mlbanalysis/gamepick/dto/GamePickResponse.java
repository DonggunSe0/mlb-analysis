package com.example.mlbanalysis.gamepick.dto;

import java.time.Instant;

public record GamePickResponse(
        Long gamePk,
        Long pickedTeamId,
        String pickedTeamName,
        Instant createdAt
) {
}
