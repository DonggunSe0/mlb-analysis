package com.example.mlbanalysis.game.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.mlbanalysis.common.config.MlbApiProperties;
import com.example.mlbanalysis.common.error.MlbApiException;
import java.net.URI;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class StatsApiMlbGameClientTest {

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
    void requestsScheduleEndpointAndDeserializesResponse() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/schedule?sportId=1&date=2026-06-01")))
                .andRespond(withSuccess("""
                        { "dates": [ { "date": "2026-06-01", "games": [ {
                          "gamePk": 822974,
                          "gameDate": "2026-06-01T22:40:00Z",
                          "officialDate": "2026-06-01",
                          "status": { "detailedState": "Final", "statusCode": "F" },
                          "teams": {
                            "away": { "team": { "id": 116, "name": "Detroit Tigers" }, "score": 10, "isWinner": true },
                            "home": { "team": { "id": 139, "name": "Tampa Bay Rays" }, "score": 9, "isWinner": false }
                          },
                          "venue": { "id": 12, "name": "Tropicana Field" }
                        } ] } ] }
                        """, MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbGameClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        var games = client.getGames(LocalDate.parse("2026-06-01"));

        assertThat(games).hasSize(1);
        assertThat(games.getFirst().gamePk()).isEqualTo(822974L);
        assertThat(games.getFirst().teams().away().team().name()).isEqualTo("Detroit Tigers");
        server.verify();
    }

    @Test
    void convertsServerFailureToMlbApiException() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/schedule?sportId=1&date=2026-06-01")))
                .andRespond(withServerError());
        var client = new StatsApiMlbGameClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        assertThatThrownBy(() -> client.getGames(LocalDate.parse("2026-06-01")))
                .isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsMalformedResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/schedule?sportId=1&date=2026-06-01")))
                .andRespond(withSuccess("{\"unexpected\":true}", MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbGameClient(restClientBuilder.baseUrl(properties.getBaseUrl()).build(), properties);

        assertThatThrownBy(() -> client.getGames(LocalDate.parse("2026-06-01")))
                .isInstanceOf(MlbApiException.class);
        server.verify();
    }
}
