package com.example.mlbanalysis.allstar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "all_star_selections")
public class AllStarSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ballot_id", nullable = false)
    private AllStarBallot ballot;

    @Column(nullable = false, length = 32)
    private String positionKey;

    @Column(nullable = false)
    private Integer playerId;

    @Column(nullable = false, length = 120)
    private String playerName;

    @Column(length = 80)
    private String teamName;

    protected AllStarSelection() {
    }

    public AllStarSelection(String positionKey, Integer playerId, String playerName, String teamName) {
        this.positionKey = positionKey;
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamName = teamName;
    }

    void setBallot(AllStarBallot ballot) { this.ballot = ballot; }
    public String getPositionKey() { return positionKey; }
    public Integer getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public String getTeamName() { return teamName; }
}
