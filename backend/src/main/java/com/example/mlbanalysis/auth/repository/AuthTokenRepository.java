package com.example.mlbanalysis.auth.repository;

import com.example.mlbanalysis.auth.entity.AuthToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByExpiresAtBefore(Instant instant);
}
