package com.example.mlbanalysis.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant expiresAt;

    protected AuthToken() {
    }

    public AuthToken(String token, AuthUser user, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public AuthUser getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
}
