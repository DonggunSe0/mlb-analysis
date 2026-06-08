package com.example.mlbanalysis.game.service;

import com.example.mlbanalysis.game.client.MlbGameClient;
import com.example.mlbanalysis.game.client.dto.MlbGameDto;
import com.example.mlbanalysis.game.client.dto.MlbGameTeamSideDto;
import com.example.mlbanalysis.game.dto.GameListResponse;
import com.example.mlbanalysis.game.dto.GameResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final MlbGameClient mlbGameClient;

    public GameService(MlbGameClient mlbGameClient) {
        this.mlbGameClient = mlbGameClient;
    }

    public GameListResponse getGames(LocalDate date) {
        return new GameListResponse(mlbGameClient.getGames(date).stream()
                .map(this::toGameResponse)
                .toList());
    }

    private GameResponse toGameResponse(MlbGameDto game) {
        MlbGameTeamSideDto home = game.teams() == null ? null : game.teams().home();
        MlbGameTeamSideDto away = game.teams() == null ? null : game.teams().away();

        return new GameResponse(
                game.gamePk(),
                game.gameDate(),
                game.status() == null ? null : game.status().detailedState(),
                teamName(home),
                teamName(away),
                home == null ? null : home.score(),
                away == null ? null : away.score()
        );
    }

    private String teamName(MlbGameTeamSideDto side) {
        return side == null || side.team() == null ? null : side.team().name();
    }
}
