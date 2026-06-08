package com.example.mlbanalysis.game.client;

import com.example.mlbanalysis.common.config.MlbApiProperties;
import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.game.client.dto.MlbGameDto;
import com.example.mlbanalysis.game.client.dto.MlbScheduleApiResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class StatsApiMlbGameClient implements MlbGameClient {

    private final RestClient restClient;
    private final MlbApiProperties properties;

    public StatsApiMlbGameClient(@Qualifier("mlbRestClient") RestClient restClient, MlbApiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public List<MlbGameDto> getGames(LocalDate date) {
        try {
            MlbScheduleApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/schedule")
                            .queryParam("sportId", properties.getSportId())
                            .queryParam("date", date)
                            .build())
                    .retrieve()
                    .body(MlbScheduleApiResponse.class);

            if (response == null || response.dates() == null) {
                throw new MlbApiException("MLB schedule response body is empty or malformed.");
            }
            return response.dates().stream()
                    .flatMap(scheduleDate -> scheduleDate.games() == null
                            ? List.<MlbGameDto>of().stream()
                            : scheduleDate.games().stream())
                    .toList();
        } catch (MlbApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new MlbApiException("Failed to retrieve MLB game data.", exception);
        }
    }
}
