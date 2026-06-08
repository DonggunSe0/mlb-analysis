package com.example.mlbanalysis.allstar.repository;

import com.example.mlbanalysis.allstar.entity.AllStarBallot;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllStarBallotRepository extends JpaRepository<AllStarBallot, Long> {
    boolean existsByUserIdAndVoteDate(Long userId, LocalDate voteDate);
    Optional<AllStarBallot> findByUserIdAndVoteDate(Long userId, LocalDate voteDate);
}
