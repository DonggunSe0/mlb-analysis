package com.example.mlbanalysis.team.client.dto;

public record MlbTeamDto(
        Integer id,
        String name,
        String abbreviation,
        String teamName,
        String locationName,
        MlbNamedResource league,
        MlbNamedResource division,
        MlbNamedResource venue,
        Boolean active
) {
}
