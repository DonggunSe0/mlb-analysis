package com.example.mlbanalysis.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.team.client.MlbTeamClient;
import com.example.mlbanalysis.team.client.dto.MlbNamedResource;
import com.example.mlbanalysis.team.client.dto.MlbStandingDivisionDto;
import com.example.mlbanalysis.team.client.dto.MlbTeamStandingDto;
import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import com.example.mlbanalysis.team.dto.TeamListResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class TeamServiceTest {

    @Test
    void mapsProviderTeamIntoPublicDto() {
        MlbTeamClient client = () -> List.of(new MlbTeamDto(
                133,
                "Athletics",
                "ATH",
                "Athletics",
                "Sacramento",
                new MlbNamedResource(103, "American League"),
                new MlbNamedResource(200, "American League West"),
                new MlbNamedResource(10, "Sutter Health Park"),
                true
        ));
        TeamService service = new TeamService(client);

        TeamListResponse response = service.getTeams();

        assertThat(response.teams()).hasSize(1);
        assertThat(response.teams().getFirst())
                .extracting(
                        "id",
                        "name",
                        "abbreviation",
                        "teamName",
                        "locationName",
                        "leagueName",
                        "divisionName",
                        "venueName",
                        "active"
                )
                .containsExactly(
                        133,
                        "Athletics",
                        "ATH",
                        "Athletics",
                        "Sacramento",
                        "American League",
                        "American League West",
                        "Sutter Health Park",
                        true
                );
    }

    @Test
    void mapsProviderStandingsIntoPublicDto() {
        MlbTeamClient client = new MlbTeamClient() {
            @Override
            public List<MlbTeamDto> getTeams() {
                return List.of();
            }

            @Override
            public List<MlbStandingDivisionDto> getStandings(String season) {
                return List.of(new MlbStandingDivisionDto(
                        new MlbNamedResource(103, null),
                        new MlbNamedResource(201, null),
                        List.of(new MlbTeamStandingDto(
                                new MlbNamedResource(139, "Rays"),
                                "2026",
                                "1",
                                "2",
                                62,
                                "-",
                                "1.0",
                                null,
                                37,
                                25,
                                42,
                                ".597",
                                true
                        ))
                ));
            }
        };

        var response = new TeamService(client).getStandings("2026");

        assertThat(response.season()).isEqualTo("2026");
        assertThat(response.standings()).hasSize(1);
        assertThat(response.standings().getFirst())
                .extracting(
                        "teamId",
                        "teamName",
                        "leagueName",
                        "divisionName",
                        "divisionRank",
                        "leagueRank",
                        "wins",
                        "losses",
                        "winningPercentage",
                        "gamesBack",
                        "wildCardGamesBack",
                        "runDifferential",
                        "divisionLeader"
                )
                .containsExactly(
                        139,
                        "Rays",
                        "American League",
                        "American League East",
                        1,
                        2,
                        37,
                        25,
                        ".597",
                        "-",
                        "1.0",
                        42,
                        true
                );
    }

    @Test
    void mapsEmptyProviderListIntoEmptyEnvelope() {
        TeamService service = new TeamService(List::of);

        TeamListResponse response = service.getTeams();

        assertThat(response.teams()).isEmpty();
    }

    @Test
    void mapsMissingNestedProviderFieldsToNulls() {
        TeamService service = new TeamService(() -> List.of(new MlbTeamDto(
                1,
                "Name",
                "NME",
                "Team Name",
                "Location",
                null,
                null,
                null,
                false
        )));

        TeamListResponse response = service.getTeams();

        assertThat(response.teams().getFirst().leagueName()).isNull();
        assertThat(response.teams().getFirst().divisionName()).isNull();
        assertThat(response.teams().getFirst().venueName()).isNull();
    }

    @Test
    void propagatesClientFailureForControllerAdvice() {
        TeamService service = new TeamService(() -> {
            throw new MlbApiException("provider unavailable");
        });

        assertThatThrownBy(service::getTeams).isInstanceOf(MlbApiException.class);
    }
}
