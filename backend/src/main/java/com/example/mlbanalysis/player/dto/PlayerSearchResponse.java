package com.example.mlbanalysis.player.dto;

import java.util.List;

public record PlayerSearchResponse(String name, List<PlayerResponse> players) {
}
