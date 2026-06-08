package com.example.mlbanalysis.allstar.repository;

import com.example.mlbanalysis.allstar.entity.AllStarBallot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AllStarBallotRepository extends JpaRepository<AllStarBallot, Long> {
    boolean existsByUserIdAndVoteDate(Long userId, LocalDate voteDate);
    Optional<AllStarBallot> findByUserIdAndVoteDate(Long userId, LocalDate voteDate);
    long countByVoteDate(LocalDate voteDate);

    @Query("""
            select selection.positionKey as positionKey,
                   selection.playerId as playerId,
                   selection.playerName as playerName,
                   selection.teamName as teamName,
                   count(selection.id) as voteCount
            from AllStarSelection selection
            join selection.ballot ballot
            where ballot.voteDate = :voteDate
            group by selection.positionKey, selection.playerId, selection.playerName, selection.teamName
            order by selection.positionKey asc, count(selection.id) desc, selection.playerName asc
            """)
    List<AllStarResultRow> summarizeResultsByVoteDate(@Param("voteDate") LocalDate voteDate);
}
