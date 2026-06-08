package com.example.mlbanalysis.game.client.dto;

import java.util.List;

public record MlbScheduleDateDto(String date, List<MlbGameDto> games) {
}
