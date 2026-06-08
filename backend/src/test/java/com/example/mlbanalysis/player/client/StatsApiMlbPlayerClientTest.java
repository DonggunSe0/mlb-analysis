package com.example.mlbanalysis.player.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.mlbanalysis.common.error.MlbApiException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class StatsApiMlbPlayerClientTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void requestsPlayerEndpointAndDeserializesResponse() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361")))
                .andRespond(withSuccess("""
                        { "people": [ {
                          "id": 545361,
                          "fullName": "Mike Trout",
                          "birthCountry": "USA",
                          "currentAge": 34,
                          "primaryPosition": { "code": "8", "name": "Outfielder", "type": "Outfielder", "abbreviation": "CF" },
                          "batSide": { "code": "R", "description": "Right" },
                          "pitchHand": { "code": "R", "description": "Right" }
                        } ] }
                        """, MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        var player = client.getPlayer(545361);

        assertThat(player.id()).isEqualTo(545361);
        assertThat(player.birthCountry()).isEqualTo("USA");
        assertThat(player.primaryPosition().name()).isEqualTo("Outfielder");
        assertThat(player.batSide().description()).isEqualTo("Right");
        assertThat(player.pitchHand().description()).isEqualTo("Right");
        server.verify();
    }


    @Test
    void requestsPlayerSearchEndpointAndDeserializesResponse() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/search?names=Mike%20Trout")))
                .andRespond(withSuccess("""
                        { "people": [ {
                          "id": 545361,
                          "fullName": "Mike Trout",
                          "birthCountry": "USA",
                          "currentAge": 34,
                          "primaryPosition": { "name": "Outfielder" },
                          "batSide": { "code": "R", "description": "Right" },
                          "pitchHand": { "code": "R", "description": "Right" }
                        } ] }
                        """, MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        var players = client.searchPlayers("Mike Trout");

        assertThat(players).hasSize(1);
        assertThat(players.getFirst().id()).isEqualTo(545361);
        assertThat(players.getFirst().batSide().description()).isEqualTo("Right");
        server.verify();
    }

    @Test
    void requestsPlayerStatsEndpointAndDeserializesSeasonStats() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361/stats?stats=season&group=hitting&season=2025")))
                .andRespond(withSuccess("""
                        { "stats": [ {
                          "type": { "displayName": "season" },
                          "group": { "displayName": "hitting" },
                          "splits": [ {
                            "season": "2025",
                            "stat": {
                              "gamesPlayed": 130,
                              "atBats": 456,
                              "runs": 73,
                              "hits": 106,
                              "doubles": 14,
                              "triples": 1,
                              "homeRuns": 26,
                              "rbi": 64,
                              "baseOnBalls": 87,
                              "strikeOuts": 178,
                              "avg": ".232",
                              "obp": ".359",
                              "slg": ".439",
                              "ops": ".798",
                              "stolenBases": 2
                            }
                          } ]
                        } ] }
                        """, MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        var stats = client.getPlayerStats(545361, "2025", "hitting");

        assertThat(stats.gamesPlayed()).isEqualTo(130);
        assertThat(stats.homeRuns()).isEqualTo(26);
        assertThat(stats.ops()).isEqualTo(".798");
        server.verify();
    }

    @Test
    void returnsNullWhenPlayerStatsHaveNoSplits() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361/stats?stats=season&group=pitching&season=2025")))
                .andRespond(withSuccess("{\"stats\":[{\"splits\":[]}]}", MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThat(client.getPlayerStats(545361, "2025", "pitching")).isNull();
        server.verify();
    }

    @Test
    void convertsPlayerStatsServerFailureToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361/stats?stats=season&group=hitting&season=2025")))
                .andRespond(withServerError());
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.getPlayerStats(545361, "2025", "hitting")).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsMalformedPlayerStatsResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361/stats?stats=season&group=hitting&season=2025")))
                .andRespond(withSuccess("{\"unexpected\":true}", MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.getPlayerStats(545361, "2025", "hitting")).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsPlayerSearchServerFailureToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/search?names=Mike%20Trout")))
                .andRespond(withServerError());
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.searchPlayers("Mike Trout")).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsMalformedPlayerSearchResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/search?names=Mike%20Trout")))
                .andRespond(withSuccess("{\"unexpected\":true}", MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.searchPlayers("Mike Trout")).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsServerFailureToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361")))
                .andRespond(withServerError());
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.getPlayer(545361)).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsMalformedResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create("https://statsapi.mlb.test/api/v1/people/545361")))
                .andRespond(withSuccess("{\"people\":[]}", MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.getPlayer(545361)).isInstanceOf(MlbApiException.class);
        server.verify();
    }
}
