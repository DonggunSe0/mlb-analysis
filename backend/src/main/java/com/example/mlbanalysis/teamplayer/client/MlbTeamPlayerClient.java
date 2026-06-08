package com.example.mlbanalysis.teamplayer.client;

import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterEntryDto;
import java.util.List;

public interface MlbTeamPlayerClient {

    List<MlbRosterEntryDto> getTeamPlayers(Integer teamId);
}
