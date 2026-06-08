package com.example.mlbanalysis.user.entity;

import com.example.mlbanalysis.auth.entity.AuthUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AuthUser user;

    @Column
    private Long favoriteTeamId;

    @Column(length = 120)
    private String favoriteTeamName;

    protected UserPreference() {
    }

    public UserPreference(AuthUser user, Long favoriteTeamId, String favoriteTeamName) {
        this.user = user;
        updateFavoriteTeam(favoriteTeamId, favoriteTeamName);
    }

    public Long getId() { return id; }
    public AuthUser getUser() { return user; }
    public Long getFavoriteTeamId() { return favoriteTeamId; }
    public String getFavoriteTeamName() { return favoriteTeamName; }

    public void updateFavoriteTeam(Long favoriteTeamId, String favoriteTeamName) {
        this.favoriteTeamId = favoriteTeamId;
        this.favoriteTeamName = favoriteTeamName == null ? null : favoriteTeamName.trim();
    }
}
