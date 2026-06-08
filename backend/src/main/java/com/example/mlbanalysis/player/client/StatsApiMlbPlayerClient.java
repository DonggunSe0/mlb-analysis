package com.example.mlbanalysis.player.client;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.player.client.dto.MlbPeopleApiResponse;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSeasonStatDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerStatsApiResponse;
import com.example.mlbanalysis.player.client.dto.MlbPlayerStatsSplitDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class StatsApiMlbPlayerClient implements MlbPlayerClient {

    private final RestClient restClient;

    public StatsApiMlbPlayerClient(@Qualifier("mlbRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public MlbPlayerDto getPlayer(Integer playerId) {
        try {
            MlbPeopleApiResponse response = restClient.get()
                    .uri("/people/{playerId}", playerId)
                    .retrieve()
                    .body(MlbPeopleApiResponse.class);

            if (response == null || response.people() == null || response.people().isEmpty()) {
                throw new MlbApiException("MLB player response body is empty or malformed.");
            }
            return response.people().getFirst();
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB player data.", exception);
        }
    }
    @Override
    public List<MlbPlayerDto> searchPlayers(String name) {
        try {
            MlbPeopleApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/people/search")
                            .queryParam("names", name)
                            .build())
                    .retrieve()
                    .body(MlbPeopleApiResponse.class);

            if (response == null || response.people() == null) {
                throw new MlbApiException("MLB player search response body is empty or malformed.");
            }
            return response.people();
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to search MLB player data.", exception);
        }
    }

    @Override
    public MlbPlayerSeasonStatDto getPlayerStats(Integer playerId, String season, String group) {
        try {
            MlbPlayerStatsApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/people/{playerId}/stats")
                            .queryParam("stats", "season")
                            .queryParam("group", group)
                            .queryParam("season", season)
                            .build(playerId))
                    .retrieve()
                    .body(MlbPlayerStatsApiResponse.class);

            if (response == null || response.stats() == null) {
                throw new MlbApiException("MLB player stats response body is empty or malformed.");
            }
            return response.stats().stream()
                    .filter(statsGroup -> statsGroup.splits() != null)
                    .flatMap(statsGroup -> statsGroup.splits().stream())
                    .filter(split -> split.stat() != null)
                    .findFirst()
                    .map(MlbPlayerStatsSplitDto::stat)
                    .orElse(null);
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB player stats data.", exception);
        }
    }

}
