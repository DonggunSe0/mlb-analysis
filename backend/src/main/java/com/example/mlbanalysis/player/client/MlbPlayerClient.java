package com.example.mlbanalysis.player.client;

import com.example.mlbanalysis.player.client.dto.MlbPlayerDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSeasonStatDto;
import java.util.List;

public interface MlbPlayerClient {

    MlbPlayerDto getPlayer(Integer playerId);

    default List<MlbPlayerDto> searchPlayers(String name) {
        throw new UnsupportedOperationException("Player search is not implemented.");
    }

    default MlbPlayerSeasonStatDto getPlayerStats(Integer playerId, String season, String group) {
        throw new UnsupportedOperationException("Player stats are not implemented.");
    }
}
