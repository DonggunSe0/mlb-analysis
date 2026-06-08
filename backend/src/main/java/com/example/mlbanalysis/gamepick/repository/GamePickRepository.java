package com.example.mlbanalysis.gamepick.repository;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.gamepick.entity.GamePick;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GamePickRepository extends JpaRepository<GamePick, Long> {
    Optional<GamePick> findByUserAndGamePk(AuthUser user, Long gamePk);
    List<GamePick> findByUserOrderByCreatedAtDesc(AuthUser user);

    @Query("""
            select pick.pickedTeamId as pickedTeamId,
                   pick.pickedTeamName as pickedTeamName,
                   count(pick.id) as pickCount
            from GamePick pick
            where pick.gamePk = :gamePk
            group by pick.pickedTeamId, pick.pickedTeamName
            order by count(pick.id) desc, pick.pickedTeamName asc
            """)
    List<GamePickSummaryRow> summarizeByGamePk(@Param("gamePk") Long gamePk);
}
