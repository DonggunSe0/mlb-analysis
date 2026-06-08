package com.example.mlbanalysis.team.client.dto;

import java.util.List;

public record MlbStandingDivisionDto(
        MlbNamedResource league,
        MlbNamedResource division,
        List<MlbTeamStandingDto> teamRecords
) {
}
