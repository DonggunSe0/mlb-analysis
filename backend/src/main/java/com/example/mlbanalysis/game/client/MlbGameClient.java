package com.example.mlbanalysis.game.client;

import com.example.mlbanalysis.game.client.dto.MlbGameDto;
import java.time.LocalDate;
import java.util.List;

public interface MlbGameClient {

    List<MlbGameDto> getGames(LocalDate date);
}
