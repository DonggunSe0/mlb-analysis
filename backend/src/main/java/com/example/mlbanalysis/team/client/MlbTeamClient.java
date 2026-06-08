package com.example.mlbanalysis.team.client;

import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import java.util.List;

public interface MlbTeamClient {

    List<MlbTeamDto> getTeams();
}
