package com.example.mlbanalysis.team.client;

import com.example.mlbanalysis.common.config.MlbApiProperties;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.team.client.dto.MlbStandingDivisionDto;
import com.example.mlbanalysis.team.client.dto.MlbStandingsApiResponse;
import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import com.example.mlbanalysis.team.client.dto.MlbTeamsApiResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class StatsApiMlbTeamClient implements MlbTeamClient {

    private final RestClient restClient;
    private final MlbApiProperties properties;

    public StatsApiMlbTeamClient(@Qualifier("mlbRestClient") RestClient restClient, MlbApiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public List<MlbTeamDto> getTeams() {
        try {
            MlbTeamsApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams")
                            .queryParam("sportId", properties.getSportId())
                            .build())
                    .retrieve()
                    .body(MlbTeamsApiResponse.class);

            if (response == null || response.teams() == null) {
                throw new MlbApiException("MLB teams response body is empty or malformed.");
            }
            return response.teams();
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB team data.", exception);
        }
    }

    @Override
    public List<MlbStandingDivisionDto> getStandings(String season) {
        try {
            MlbStandingsApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/standings")
                            .queryParam("leagueId", "103,104")
                            .queryParam("season", season)
                            .queryParam("standingsTypes", "regularSeason")
                            .build())
                    .retrieve()
                    .body(MlbStandingsApiResponse.class);

            if (response == null || response.records() == null) {
                throw new MlbApiException("MLB standings response body is empty or malformed.");
            }
            return response.records();
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB standings data.", exception);
        }
    }
}
