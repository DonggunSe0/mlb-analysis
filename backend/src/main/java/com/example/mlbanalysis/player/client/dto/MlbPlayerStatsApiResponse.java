package com.example.mlbanalysis.player.client.dto;

import java.util.List;

public record MlbPlayerStatsApiResponse(List<MlbPlayerStatsGroupDto> stats) {
}
