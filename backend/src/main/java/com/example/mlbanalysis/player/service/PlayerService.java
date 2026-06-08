package com.example.mlbanalysis.player.service;

import com.example.mlbanalysis.player.client.MlbPlayerClient;
import com.example.mlbanalysis.player.client.dto.MlbPlayerDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerPositionDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSideDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSeasonStatDto;
import com.example.mlbanalysis.player.dto.PlayerResponse;
import com.example.mlbanalysis.player.dto.PlayerSearchResponse;
import com.example.mlbanalysis.player.dto.PlayerStatsResponse;
import java.time.Year;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final MlbPlayerClient mlbPlayerClient;

    public PlayerService(MlbPlayerClient mlbPlayerClient) {
        this.mlbPlayerClient = mlbPlayerClient;
    }

    @Cacheable(value = "mlbPlayers", key = "#playerId")
    public PlayerResponse getPlayer(Integer playerId) {
        return toPlayerResponse(mlbPlayerClient.getPlayer(playerId));
    }

    @Cacheable(value = "mlbPlayerStats", key = "#playerId + ':' + (#season == null ? '' : #season) + ':' + (#group == null ? '' : #group)")
    public PlayerStatsResponse getPlayerStats(Integer playerId, String season, String group) {
        String resolvedSeason = season == null || season.isBlank() ? String.valueOf(Year.now().getValue()) : season;
        String resolvedGroup = group == null || group.isBlank() ? "hitting" : group;
        return toPlayerStatsResponse(
                playerId,
                resolvedSeason,
                resolvedGroup,
                mlbPlayerClient.getPlayerStats(playerId, resolvedSeason, resolvedGroup)
        );
    }


    @Cacheable(value = "mlbPlayerSearches", key = "#name == null ? '' : #name.trim().toLowerCase()")
    public PlayerSearchResponse searchPlayers(String name) {
        return new PlayerSearchResponse(name, mlbPlayerClient.searchPlayers(name).stream()
                .map(this::toPlayerResponse)
                .toList());
    }

    private PlayerResponse toPlayerResponse(MlbPlayerDto player) {
        return new PlayerResponse(
                player.id(),
                player.fullName(),
                player.birthCountry(),
                player.currentAge(),
                positionName(player.primaryPosition()),
                descriptionOf(player.batSide()),
                descriptionOf(player.pitchHand()),
                headshotUrl(player.id())
        );
    }

    private PlayerStatsResponse toPlayerStatsResponse(Integer playerId, String season, String group, MlbPlayerSeasonStatDto stat) {
        if (stat == null) {
            return new PlayerStatsResponse(
                    playerId, season, group,
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null
            );
        }
        return new PlayerStatsResponse(
                playerId,
                season,
                group,
                stat.gamesPlayed(),
                stat.atBats(),
                stat.runs(),
                stat.hits(),
                stat.doubles(),
                stat.triples(),
                stat.homeRuns(),
                stat.rbi(),
                stat.baseOnBalls(),
                stat.strikeOuts(),
                stat.avg(),
                stat.obp(),
                stat.slg(),
                stat.ops(),
                stat.stolenBases()
        );
    }

    private String headshotUrl(Integer playerId) {
        if (playerId == null) {
            return null;
        }
        return "https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/"
                + playerId
                + "/headshot/67/current";
    }

    private String positionName(MlbPlayerPositionDto position) {
        return position == null ? null : position.name();
    }

    private String descriptionOf(MlbPlayerSideDto side) {
        return side == null ? null : side.description();
    }
}
