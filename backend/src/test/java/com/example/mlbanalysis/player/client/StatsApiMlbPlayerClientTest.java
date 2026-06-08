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
