package com.example.mlbanalysis.team.client.dto;

public record MlbTeamStandingDto(
        MlbNamedResource team,
        String season,
        String divisionRank,
        String leagueRank,
        Integer gamesPlayed,
        String gamesBack,
        String wildCardGamesBack,
        MlbWinLossRecordDto leagueRecord,
        Integer wins,
        Integer losses,
        Integer runDifferential,
        String winningPercentage,
        Boolean divisionLeader
) {
}
