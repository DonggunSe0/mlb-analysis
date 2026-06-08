package com.example.mlbanalysis.team.client.dto;

import java.util.List;

public record MlbStandingsApiResponse(List<MlbStandingDivisionDto> records) {
}
