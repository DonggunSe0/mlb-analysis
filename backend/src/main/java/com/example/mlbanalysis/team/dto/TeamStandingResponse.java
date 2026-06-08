package com.example.mlbanalysis.team.dto;

public record TeamStandingResponse(
        Integer teamId,
        String teamName,
        String season,
        String leagueName,
        String divisionName,
        Integer divisionRank,
        Integer leagueRank,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        String winningPercentage,
        String gamesBack,
        String wildCardGamesBack,
        Integer runDifferential,
        Boolean divisionLeader
) {
}
