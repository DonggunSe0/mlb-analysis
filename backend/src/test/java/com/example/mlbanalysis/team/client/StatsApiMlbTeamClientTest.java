package com.example.mlbanalysis.team.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.mlbanalysis.common.config.MlbApiProperties;
import com.example.mlbanalysis.common.error.MlbApiException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class StatsApiMlbTeamClientTest {

    private MlbApiProperties properties;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        properties = new MlbApiProperties();
        properties.setBaseUrl("https://statsapi.mlb.test/api/v1");
        properties.setSportId(1);
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void requestsTeamsEndpointAndDeserializesResponse() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/teams?sportId=1")))
                .andRespond(withSuccess("""
                        {
                          "teams": [
                            {
                              "id": 133,
                              "name": "Athletics",
                              "abbreviation": "ATH",
                              "teamName": "Athletics",
                              "locationName": "Sacramento",
                              "league": { "id": 103, "name": "American League" },
                              "division": { "id": 200, "name": "American League West" },
                              "venue": { "id": 10, "name": "Sutter Health Park" },
                              "active": true
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        StatsApiMlbTeamClient client = new StatsApiMlbTeamClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        var teams = client.getTeams();

        assertThat(teams).hasSize(1);
        assertThat(teams.getFirst().id()).isEqualTo(133);
        assertThat(teams.getFirst().league().name()).isEqualTo("American League");
        server.verify();
    }

    @Test
    void requestsStandingsEndpointAndDeserializesResponse() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/standings?leagueId=103,104&season=2026&standingsTypes=regularSeason")))
                .andRespond(withSuccess("""
                        {
                          "records": [ {
                            "league": { "id": 103, "link": "/api/v1/league/103" },
                            "division": { "id": 201, "link": "/api/v1/divisions/201" },
                            "teamRecords": [ {
                              "team": { "id": 139, "name": "Rays", "link": "/api/v1/teams/139" },
                              "season": "2026",
                              "divisionRank": "1",
                              "leagueRank": "1",
                              "gamesPlayed": 62,
                              "gamesBack": "-",
                              "wildCardGamesBack": "-",
                              "leagueRecord": { "wins": 37, "losses": 25, "ties": 0, "pct": ".597" },
                              "wins": 37,
                              "losses": 25,
                              "runDifferential": 42,
                              "winningPercentage": ".597",
                              "divisionLeader": true
                            } ]
                          } ]
                        }
                        """, MediaType.APPLICATION_JSON));
        StatsApiMlbTeamClient client = new StatsApiMlbTeamClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        var standings = client.getStandings("2026");

        assertThat(standings).hasSize(1);
        assertThat(standings.getFirst().division().id()).isEqualTo(201);
        assertThat(standings.getFirst().teamRecords().getFirst().team().name()).isEqualTo("Rays");
        assertThat(standings.getFirst().teamRecords().getFirst().wins()).isEqualTo(37);
        server.verify();
    }

    @Test
    void convertsMalformedStandingsResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/standings?leagueId=103,104&season=2026&standingsTypes=regularSeason")))
                .andRespond(withSuccess("{\"unexpected\":true}", MediaType.APPLICATION_JSON));
        StatsApiMlbTeamClient client = new StatsApiMlbTeamClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        assertThatThrownBy(() -> client.getStandings("2026")).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsServerFailureToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/teams?sportId=1")))
                .andRespond(withServerError());
        StatsApiMlbTeamClient client = new StatsApiMlbTeamClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        assertThatThrownBy(client::getTeams).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsMalformedResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/teams?sportId=1")))
                .andRespond(withSuccess("{\"unexpected\":true}", MediaType.APPLICATION_JSON));
        StatsApiMlbTeamClient client = new StatsApiMlbTeamClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        assertThatThrownBy(client::getTeams).isInstanceOf(MlbApiException.class);
        server.verify();
    }
}
