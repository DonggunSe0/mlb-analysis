package com.example.mlbanalysis.auth.repository;

import com.example.mlbanalysis.auth.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    boolean existsByEmail(String email);
    Optional<AuthUser> findByEmail(String email);
}
