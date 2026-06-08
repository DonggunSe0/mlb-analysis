package com.example.mlbanalysis.gamepick.entity;

import com.example.mlbanalysis.auth.entity.AuthUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "game_picks",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_pick_user_game", columnNames = {"user_id", "game_pk"})
)
public class GamePick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(name = "game_pk", nullable = false)
    private Long gamePk;

    @Column(nullable = false)
    private Long pickedTeamId;

    @Column(nullable = false, length = 120)
    private String pickedTeamName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected GamePick() {
    }

    public GamePick(AuthUser user, Long gamePk, Long pickedTeamId, String pickedTeamName, Instant createdAt) {
        this.user = user;
        this.gamePk = gamePk;
        this.pickedTeamId = pickedTeamId;
        this.pickedTeamName = pickedTeamName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public AuthUser getUser() { return user; }
    public Long getGamePk() { return gamePk; }
    public Long getPickedTeamId() { return pickedTeamId; }
    public String getPickedTeamName() { return pickedTeamName; }
    public Instant getCreatedAt() { return createdAt; }

    public void updatePick(Long pickedTeamId, String pickedTeamName) {
        this.pickedTeamId = pickedTeamId;
        this.pickedTeamName = pickedTeamName;
    }
}
