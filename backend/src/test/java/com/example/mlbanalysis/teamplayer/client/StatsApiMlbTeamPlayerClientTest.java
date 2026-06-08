package com.example.mlbanalysis.teamplayer.client;

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

class StatsApiMlbTeamPlayerClientTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void requestsRosterEndpointAndDeserializesResponse() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/teams/133/roster?rosterType=active")))
                .andRespond(withSuccess("""
                        { "roster": [ {
                          "person": { "id": 675961, "fullName": "Alika Williams" },
                          "jerseyNumber": "12",
                          "position": { "code": "4", "name": "Second Base", "type": "Infielder", "abbreviation": "2B" },
                          "status": { "code": "A", "description": "Active" }
                        } ] }
                        """, MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbTeamPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        var roster = client.getTeamPlayers(133);

        assertThat(roster).hasSize(1);
        assertThat(roster.getFirst().person().fullName()).isEqualTo("Alika Williams");
        assertThat(roster.getFirst().position().abbreviation()).isEqualTo("2B");
        server.verify();
    }

    @Test
    void convertsServerFailureToMlbApiException() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/teams/133/roster?rosterType=active")))
                .andRespond(withServerError());
        var client = new StatsApiMlbTeamPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.getTeamPlayers(133)).isInstanceOf(MlbApiException.class);
        server.verify();
    }

    @Test
    void convertsMalformedResponseToMlbApiException() {
        server.expect(once(), requestTo(URI.create(
                        "https://statsapi.mlb.test/api/v1/teams/133/roster?rosterType=active")))
                .andRespond(withSuccess("{\"unexpected\":true}", MediaType.APPLICATION_JSON));
        var client = new StatsApiMlbTeamPlayerClient(restClientBuilder.baseUrl("https://statsapi.mlb.test/api/v1").build());

        assertThatThrownBy(() -> client.getTeamPlayers(133)).isInstanceOf(MlbApiException.class);
        server.verify();
    }
}
