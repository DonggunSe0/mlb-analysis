package com.example.mlbanalysis.team.dto;

public record TeamResponse(
        Integer id,
        String name,
        String abbreviation,
        String teamName,
        String locationName,
        String leagueName,
        String divisionName,
        String venueName,
        Boolean active
) {
}
