package com.example.mlbanalysis.player.service;

import com.example.mlbanalysis.player.client.MlbPlayerClient;
import com.example.mlbanalysis.player.client.dto.MlbPlayerDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerPositionDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSideDto;
import com.example.mlbanalysis.player.dto.PlayerResponse;
import com.example.mlbanalysis.player.dto.PlayerSearchResponse;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final MlbPlayerClient mlbPlayerClient;

    public PlayerService(MlbPlayerClient mlbPlayerClient) {
        this.mlbPlayerClient = mlbPlayerClient;
    }

    public PlayerResponse getPlayer(Integer playerId) {
        return toPlayerResponse(mlbPlayerClient.getPlayer(playerId));
    }


    public PlayerSearchResponse searchPlayers(String name) {
        return new PlayerSearchResponse(name, mlbPlayerClient.searchPlayers(name).stream()
                .map(this::toPlayerResponse)
                .toList());
    }

    private PlayerResponse toPlayerResponse(MlbPlayerDto player) {
        return new PlayerResponse(
                player.id(),
                player.fullName(),
                player.birthCountry(),
                player.currentAge(),
                positionName(player.primaryPosition()),
                descriptionOf(player.batSide()),
                descriptionOf(player.pitchHand())
        );
    }

    private String positionName(MlbPlayerPositionDto position) {
        return position == null ? null : position.name();
    }

    private String descriptionOf(MlbPlayerSideDto side) {
        return side == null ? null : side.description();
    }
}
