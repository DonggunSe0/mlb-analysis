package com.example.mlbanalysis.player.dto;

import java.util.List;

public record PlayerBrowseResponse(
        List<PlayerResponse> players,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<String> countries,
        List<String> positions,
        List<PlayerTeamOptionResponse> teams
) {
}
