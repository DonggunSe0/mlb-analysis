package com.example.mlbanalysis.team.client;

import com.example.mlbanalysis.team.client.dto.MlbStandingDivisionDto;
import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import java.util.List;

public interface MlbTeamClient {

    List<MlbTeamDto> getTeams();

    default List<MlbStandingDivisionDto> getStandings(String season) {
        throw new UnsupportedOperationException("Standings are not supported by this MLB team client.");
    }
}
