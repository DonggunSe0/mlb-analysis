package com.example.mlbanalysis.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.player.client.dto.MlbPlayerDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerPositionDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSideDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSeasonStatDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerTeamDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlayerServiceTest {

    @Test
    void mapsProviderPlayerIntoPublicDto() {
        var service = new PlayerService(playerId -> new MlbPlayerDto(
                545361,
                "Mike Trout",
                "USA",
                34,
                new MlbPlayerPositionDto("Outfielder"),
                new MlbPlayerSideDto("R", "Right"),
                new MlbPlayerSideDto("R", "Right")
        ));

        var response = service.getPlayer(545361);

        assertThat(response.id()).isEqualTo(545361);
        assertThat(response.fullName()).isEqualTo("Mike Trout");
        assertThat(response.birthCountry()).isEqualTo("USA");
        assertThat(response.currentAge()).isEqualTo(34);
        assertThat(response.primaryPosition()).isEqualTo("Outfielder");
        assertThat(response.batSide()).isEqualTo("Right");
        assertThat(response.pitchHand()).isEqualTo("Right");
        assertThat(response.headshotUrl()).isEqualTo("https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/545361/headshot/67/current");
    }


    @Test
    void mapsPlayerSearchResultsIntoPublicEnvelope() {
        var service = new PlayerService(new com.example.mlbanalysis.player.client.MlbPlayerClient() {
            @Override
            public MlbPlayerDto getPlayer(Integer playerId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<MlbPlayerDto> searchPlayers(String name) {
                return List.of(new MlbPlayerDto(
                        545361,
                        "Mike Trout",
                        "USA",
                        34,
                        new MlbPlayerPositionDto("Outfielder"),
                        new MlbPlayerSideDto("R", "Right"),
                        new MlbPlayerSideDto("R", "Right")
                ));
            }
        });

        var response = service.searchPlayers("Mike Trout");

        assertThat(response.name()).isEqualTo("Mike Trout");
        assertThat(response.players()).hasSize(1);
        assertThat(response.players().getFirst().id()).isEqualTo(545361);
        assertThat(response.players().getFirst().fullName()).isEqualTo("Mike Trout");
    }


    @Test
    void browsesActivePlayersAlphabeticallyWithDefaultPagination() {
        var service = new PlayerService(new BrowseClient(List.of(
                player(3, "Carlos Correa", "Puerto Rico", true, 118, "Minnesota Twins", "Shortstop", "6", "SS"),
                player(1, "Andrew Abbott", "USA", true, 113, "Cincinnati Reds", "Pitcher", "1", "P"),
                player(2, "Bo Bichette", "USA", true, 141, "Toronto Blue Jays", "Shortstop", "6", "SS"),
                player(99, "Retired Player", "USA", false, 113, "Cincinnati Reds", "Pitcher", "1", "P")
        )));

        var response = service.browsePlayers("2026", null, null, null, null, null, null);

        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.players()).extracting("fullName").containsExactly("Andrew Abbott", "Bo Bichette", "Carlos Correa");
        assertThat(response.countries()).containsExactly("Puerto Rico", "USA");
        assertThat(response.positions()).containsExactly("Pitcher", "Shortstop");
        assertThat(response.teams()).extracting("id").containsExactly(113, 118, 141);
    }

    @Test
    void browsesPlayersWithCombinedFiltersAndPagination() {
        var service = new PlayerService(new BrowseClient(List.of(
                player(1, "Andrew Abbott", "USA", true, 113, "Cincinnati Reds", "Pitcher", "1", "P"),
                player(2, "Austin Riley", "USA", true, 144, "Atlanta Braves", "Third Baseman", "5", "3B"),
                player(3, "Bo Bichette", "USA", true, 141, "Toronto Blue Jays", "Shortstop", "6", "SS"),
                player(4, "Bobby Witt Jr.", "USA", true, 118, "Kansas City Royals", "Shortstop", "6", "SS")
        )));

        var response = service.browsePlayers("2026", "Bo", "usa", null, "SS", 0, 1);

        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.players()).extracting("fullName").containsExactly("Bo Bichette");
        assertThat(response.last()).isFalse();

        var teamFiltered = service.browsePlayers("2026", null, null, 118, "Shortstop", 0, 20);
        assertThat(teamFiltered.players()).extracting("fullName").containsExactly("Bobby Witt Jr.");
    }

    @Test
    void mapsPlayerSeasonStatsIntoPublicDto() {
        var service = new PlayerService(new com.example.mlbanalysis.player.client.MlbPlayerClient() {
            @Override
            public MlbPlayerDto getPlayer(Integer playerId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public MlbPlayerSeasonStatDto getPlayerStats(Integer playerId, String season, String group) {
                return new MlbPlayerSeasonStatDto(
                        130, 567, 456, 73, 106, 14, 1, 26, 64, 87, 6, 178,
                        ".232", ".359", ".439", ".798", 2, 1, ".667", 200, 10, 9, 0, 4, 2450, ".301", 120, 150
                );
            }
        });

        var response = service.getPlayerStats(545361, "2025", "hitting");

        assertThat(response.playerId()).isEqualTo(545361);
        assertThat(response.season()).isEqualTo("2025");
        assertThat(response.group()).isEqualTo("hitting");
        assertThat(response.gamesPlayed()).isEqualTo(130);
        assertThat(response.homeRuns()).isEqualTo(26);
        assertThat(response.ops()).isEqualTo(".798");
    }

    @Test
    void mapsMissingPlayerSeasonStatsToEmptyPublicDto() {
        var service = new PlayerService(new com.example.mlbanalysis.player.client.MlbPlayerClient() {
            @Override
            public MlbPlayerDto getPlayer(Integer playerId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public MlbPlayerSeasonStatDto getPlayerStats(Integer playerId, String season, String group) {
                return null;
            }
        });

        var response = service.getPlayerStats(545361, "2025", "pitching");

        assertThat(response.playerId()).isEqualTo(545361);
        assertThat(response.season()).isEqualTo("2025");
        assertThat(response.group()).isEqualTo("pitching");
        assertThat(response.gamesPlayed()).isNull();
        assertThat(response.ops()).isNull();
    }

    @Test
    void handlesMissingNestedFieldsWithoutNpe() {
        var response = new PlayerService(playerId -> new MlbPlayerDto(
                1, "Unknown Player", null, null, null, null, null
        )).getPlayer(1);

        assertThat(response.primaryPosition()).isNull();
        assertThat(response.batSide()).isNull();
        assertThat(response.pitchHand()).isNull();
    }

    @Test
    void propagatesClientFailureForControllerAdvice() {
        PlayerService service = new PlayerService(playerId -> {
            throw new MlbApiException("provider unavailable");
        });

        assertThatThrownBy(() -> service.getPlayer(545361)).isInstanceOf(MlbApiException.class);
    }

    private static MlbPlayerDto player(
            Integer id,
            String name,
            String country,
            Boolean active,
            Integer teamId,
            String teamName,
            String positionName,
            String positionCode,
            String positionAbbreviation
    ) {
        return new MlbPlayerDto(
                id,
                name,
                country,
                27,
                active,
                new MlbPlayerTeamDto(teamId, teamName),
                new MlbPlayerPositionDto(positionCode, positionName, null, positionAbbreviation),
                new MlbPlayerSideDto("R", "Right"),
                new MlbPlayerSideDto("R", "Right")
        );
    }

    private record BrowseClient(List<MlbPlayerDto> players) implements com.example.mlbanalysis.player.client.MlbPlayerClient {
        @Override
        public MlbPlayerDto getPlayer(Integer playerId) {
            return players.stream().filter(player -> player.id().equals(playerId)).findFirst().orElseThrow();
        }

        @Override
        public List<MlbPlayerDto> getPlayers(String season) {
            return players;
        }
    }

}
