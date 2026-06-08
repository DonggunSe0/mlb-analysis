package com.example.mlbanalysis.common.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final String MLB_GAMES = "mlbGames";
    public static final String MLB_PLAYERS = "mlbPlayers";
    public static final String MLB_PLAYER_STATS = "mlbPlayerStats";
    public static final String MLB_PLAYER_SEARCHES = "mlbPlayerSearches";
    public static final String MLB_TEAMS = "mlbTeams";
    public static final String MLB_TEAM_PLAYERS = "mlbTeamPlayers";
    public static final String MLB_STANDINGS = "mlbStandings";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                MLB_GAMES,
                MLB_PLAYERS,
                MLB_PLAYER_STATS,
                MLB_PLAYER_SEARCHES,
                MLB_TEAMS,
                MLB_TEAM_PLAYERS,
                MLB_STANDINGS
        );
    }
}
