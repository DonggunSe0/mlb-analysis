package com.example.mlbanalysis.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.game.client.MlbGameClient;
import com.example.mlbanalysis.game.client.dto.MlbGameDto;
import com.example.mlbanalysis.game.client.dto.MlbGameStatusDto;
import com.example.mlbanalysis.game.client.dto.MlbGameTeamDto;
import com.example.mlbanalysis.game.client.dto.MlbGameTeamSideDto;
import com.example.mlbanalysis.game.client.dto.MlbGameTeamsDto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameServiceTest {

    @Test
    void mapsProviderGameIntoPublicDto() {
        MlbGameClient client = date -> List.of(new MlbGameDto(
                822974L,
                "2026-06-01T22:40:00Z",
                new MlbGameStatusDto("Final"),
                new MlbGameTeamsDto(
                        new MlbGameTeamSideDto(new MlbGameTeamDto("Detroit Tigers"), 10),
                        new MlbGameTeamSideDto(new MlbGameTeamDto("Tampa Bay Rays"), 9)
                )
        ));

        var response = new GameService(client).getGames(LocalDate.parse("2026-06-01"));

        assertThat(response.games()).hasSize(1);
        assertThat(response.games().getFirst().gamePk()).isEqualTo(822974L);
        assertThat(response.games().getFirst().gameDate()).isEqualTo("2026-06-01T22:40:00Z");
        assertThat(response.games().getFirst().status()).isEqualTo("Final");
        assertThat(response.games().getFirst().awayTeam()).isEqualTo("Detroit Tigers");
        assertThat(response.games().getFirst().homeTeam()).isEqualTo("Tampa Bay Rays");
        assertThat(response.games().getFirst().awayScore()).isEqualTo(10);
        assertThat(response.games().getFirst().homeScore()).isEqualTo(9);
    }

    @Test
    void mapsEmptyGameListIntoEmptyEnvelope() {
        var response = new GameService(date -> List.of()).getGames(LocalDate.parse("2026-06-01"));

        assertThat(response.games()).isEmpty();
    }

    @Test
    void handlesMissingNestedFieldsWithoutNpe() {
        var response = new GameService(date -> List.of(new MlbGameDto(
                1L, null, null, null
        ))).getGames(LocalDate.parse("2026-06-01"));

        assertThat(response.games().getFirst().status()).isNull();
        assertThat(response.games().getFirst().awayTeam()).isNull();
        assertThat(response.games().getFirst().homeTeam()).isNull();
        assertThat(response.games().getFirst().awayScore()).isNull();
        assertThat(response.games().getFirst().homeScore()).isNull();
    }

    @Test
    void propagatesClientFailureForControllerAdvice() {
        GameService service = new GameService(date -> {
            throw new MlbApiException("provider unavailable");
        });

        assertThatThrownBy(() -> service.getGames(LocalDate.parse("2026-06-01")))
                .isInstanceOf(MlbApiException.class);
    }
}
