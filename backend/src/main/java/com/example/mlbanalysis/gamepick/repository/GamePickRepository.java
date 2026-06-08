package com.example.mlbanalysis.gamepick.repository;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.gamepick.entity.GamePick;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePickRepository extends JpaRepository<GamePick, Long> {
    Optional<GamePick> findByUserAndGamePk(AuthUser user, Long gamePk);
    List<GamePick> findByUserOrderByCreatedAtDesc(AuthUser user);
}
