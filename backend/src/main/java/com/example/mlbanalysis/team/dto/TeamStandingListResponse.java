package com.example.mlbanalysis.team.dto;

import java.util.List;

public record TeamStandingListResponse(String season, List<TeamStandingResponse> standings) {
}
