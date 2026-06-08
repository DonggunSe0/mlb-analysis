package com.example.mlbanalysis.teamplayer.dto;

import java.util.List;

public record TeamPlayerListResponse(Integer teamId, List<TeamPlayerResponse> players) {
}
