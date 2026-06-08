package com.example.mlbanalysis.gamepick.repository;

public interface GamePickSummaryRow {
    Long getPickedTeamId();
    String getPickedTeamName();
    Long getPickCount();
}
