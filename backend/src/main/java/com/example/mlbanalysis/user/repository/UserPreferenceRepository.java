package com.example.mlbanalysis.user.repository;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.user.entity.UserPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUser(AuthUser user);
}
