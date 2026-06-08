package com.example.mlbanalysis.teamplayer.client;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterApiResponse;
import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterEntryDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class StatsApiMlbTeamPlayerClient implements MlbTeamPlayerClient {

    private final RestClient restClient;

    public StatsApiMlbTeamPlayerClient(@Qualifier("mlbRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<MlbRosterEntryDto> getTeamPlayers(Integer teamId) {
        try {
            MlbRosterApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams/{teamId}/roster")
                            .queryParam("rosterType", "active")
                            .build(teamId))
                    .retrieve()
                    .body(MlbRosterApiResponse.class);

            if (response == null || response.roster() == null) {
                throw new MlbApiException("MLB team roster response body is empty or malformed.");
            }
            return response.roster();
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB team roster data.", exception);
        }
    }
}
