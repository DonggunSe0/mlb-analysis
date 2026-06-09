package com.example.mlbanalysis.player.service;

import com.example.mlbanalysis.player.client.MlbPlayerClient;
import com.example.mlbanalysis.player.client.dto.MlbPlayerDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerPositionDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSideDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerSeasonStatDto;
import com.example.mlbanalysis.player.client.dto.MlbPlayerTeamDto;
import com.example.mlbanalysis.player.dto.PlayerBrowseResponse;
import com.example.mlbanalysis.player.dto.PlayerResponse;
import com.example.mlbanalysis.player.dto.PlayerSearchResponse;
import com.example.mlbanalysis.player.dto.PlayerStatsResponse;
import com.example.mlbanalysis.player.dto.PlayerTeamOptionResponse;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

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
        String resolvedSeason = resolveSeason(season);
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

    @Cacheable(value = "mlbPlayerBrowses", key = "(#season == null ? '' : #season) + ':' + (#query == null ? '' : #query.trim().toLowerCase()) + ':' + (#country == null ? '' : #country.trim().toLowerCase()) + ':' + (#teamId == null ? '' : #teamId) + ':' + (#position == null ? '' : #position.trim().toLowerCase()) + ':' + (#page == null ? '' : #page) + ':' + (#size == null ? '' : #size)")
    public PlayerBrowseResponse browsePlayers(String season, String query, String country, Integer teamId, String position, Integer page, Integer size) {
        String resolvedSeason = resolveSeason(season);
        int resolvedPage = Math.max(0, page == null ? 0 : page);
        int resolvedSize = Math.max(1, Math.min(MAX_PAGE_SIZE, size == null ? DEFAULT_PAGE_SIZE : size));

        List<MlbPlayerDto> activePlayers = mlbPlayerClient.getPlayers(resolvedSeason).stream()
                .filter(this::isActivePlayer)
                .sorted(Comparator.comparing(player -> normalizeSort(player.fullName())))
                .toList();

        List<String> countries = activePlayers.stream()
                .map(MlbPlayerDto::birthCountry)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<String> positions = activePlayers.stream()
                .map(player -> positionName(player.primaryPosition()))
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<PlayerTeamOptionResponse> teams = activePlayers.stream()
                .map(MlbPlayerDto::currentTeam)
                .filter(team -> team != null && team.id() != null)
                .collect(java.util.stream.Collectors.toMap(
                        MlbPlayerTeamDto::id,
                        team -> new PlayerTeamOptionResponse(team.id(), team.name()),
                        (first, ignored) -> first
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(PlayerTeamOptionResponse::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(PlayerTeamOptionResponse::id, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        List<PlayerResponse> filtered = activePlayers.stream()
                .filter(player -> matchesQuery(player, query))
                .filter(player -> matchesCountry(player, country))
                .filter(player -> matchesTeam(player, teamId))
                .filter(player -> matchesPosition(player, position))
                .map(this::toPlayerResponse)
                .toList();

        long totalElements = filtered.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil(totalElements / (double) resolvedSize);
        int fromIndex = Math.min(resolvedPage * resolvedSize, filtered.size());
        int toIndex = Math.min(fromIndex + resolvedSize, filtered.size());
        List<PlayerResponse> pagePlayers = filtered.subList(fromIndex, toIndex);

        return new PlayerBrowseResponse(
                pagePlayers,
                resolvedPage,
                resolvedSize,
                totalElements,
                totalPages,
                resolvedPage == 0,
                totalPages == 0 || resolvedPage >= totalPages - 1,
                countries,
                positions,
                teams
        );
    }

    private boolean isActivePlayer(MlbPlayerDto player) {
        return player != null
                && player.id() != null
                && player.fullName() != null
                && !player.fullName().isBlank()
                && !Boolean.FALSE.equals(player.active());
    }

    private boolean matchesQuery(MlbPlayerDto player, String query) {
        if (query == null || query.isBlank()) return true;
        return containsIgnoreCase(player.fullName(), query);
    }

    private boolean matchesCountry(MlbPlayerDto player, String country) {
        if (country == null || country.isBlank()) return true;
        return equalsIgnoreCase(player.birthCountry(), country);
    }

    private boolean matchesTeam(MlbPlayerDto player, Integer teamId) {
        if (teamId == null) return true;
        return player.currentTeam() != null && Objects.equals(player.currentTeam().id(), teamId);
    }

    private boolean matchesPosition(MlbPlayerDto player, String position) {
        if (position == null || position.isBlank()) return true;
        MlbPlayerPositionDto primaryPosition = player.primaryPosition();
        return primaryPosition != null
                && (equalsIgnoreCase(primaryPosition.name(), position)
                || equalsIgnoreCase(primaryPosition.code(), position)
                || equalsIgnoreCase(primaryPosition.abbreviation(), position));
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    private String normalizeSort(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String resolveSeason(String season) {
        return season == null || season.isBlank() ? String.valueOf(Year.now().getValue()) : season.trim();
    }

    private PlayerResponse toPlayerResponse(MlbPlayerDto player) {
        return new PlayerResponse(
                player.id(),
                player.fullName(),
                player.birthCountry(),
                player.currentAge(),
                positionName(player.primaryPosition()),
                positionCode(player.primaryPosition()),
                positionAbbreviation(player.primaryPosition()),
                player.currentTeam() == null ? null : player.currentTeam().id(),
                player.currentTeam() == null ? null : player.currentTeam().name(),
                descriptionOf(player.batSide()),
                descriptionOf(player.pitchHand()),
                headshotUrl(player.id())
        );
    }

    private PlayerStatsResponse toPlayerStatsResponse(Integer playerId, String season, String group, MlbPlayerSeasonStatDto stat) {
        if (stat == null) {
            return new PlayerStatsResponse(
                    playerId, season, group, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
            );
        }
        return new PlayerStatsResponse(
                playerId,
                season,
                group,
                stat.gamesPlayed(),
                stat.plateAppearances(),
                stat.atBats(),
                stat.runs(),
                stat.hits(),
                stat.doubles(),
                stat.triples(),
                stat.homeRuns(),
                stat.rbi(),
                stat.baseOnBalls(),
                stat.intentionalWalks(),
                stat.strikeOuts(),
                stat.avg(),
                stat.obp(),
                stat.slg(),
                stat.ops(),
                stat.stolenBases(),
                stat.caughtStealing(),
                stat.stolenBasePercentage(),
                stat.totalBases(),
                stat.hitByPitch(),
                stat.groundIntoDoublePlay(),
                stat.sacBunts(),
                stat.sacFlies(),
                stat.numberOfPitches(),
                stat.babip(),
                stat.groundOuts(),
                stat.airOuts(),
                stat.gamesStarted(),
                stat.era(),
                stat.inningsPitched(),
                stat.wins(),
                stat.losses(),
                stat.saves(),
                stat.holds(),
                stat.blownSaves(),
                stat.earnedRuns(),
                stat.whip(),
                stat.battersFaced(),
                stat.gamesPitched(),
                stat.completeGames(),
                stat.shutouts(),
                stat.strikePercentage(),
                stat.wildPitches(),
                stat.pitchesPerInning(),
                stat.strikeoutWalkRatio(),
                stat.strikeoutsPer9Inn(),
                stat.walksPer9Inn(),
                stat.hitsPer9Inn(),
                stat.homeRunsPer9()
        );
    }

    private String headshotUrl(Integer playerId) {
        if (playerId == null) return null;
        return "https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/"
                + playerId
                + "/headshot/67/current";
    }

    private String positionName(MlbPlayerPositionDto position) {
        return position == null ? null : position.name();
    }

    private String positionCode(MlbPlayerPositionDto position) {
        return position == null ? null : position.code();
    }

    private String positionAbbreviation(MlbPlayerPositionDto position) {
        return position == null ? null : position.abbreviation();
    }

    private String descriptionOf(MlbPlayerSideDto side) {
        return side == null ? null : side.description();
    }
}
