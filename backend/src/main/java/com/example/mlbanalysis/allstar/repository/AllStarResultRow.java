package com.example.mlbanalysis.allstar.repository;

public interface AllStarResultRow {
    String getPositionKey();
    Integer getPlayerId();
    String getPlayerName();
    String getTeamName();
    Long getVoteCount();
}
