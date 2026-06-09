package com.example.mlbanalysis.player.client.dto;

public record MlbPlayerPositionDto(String code, String name, String type, String abbreviation) {
    public MlbPlayerPositionDto(String name) {
        this(null, name, null, null);
    }
}
