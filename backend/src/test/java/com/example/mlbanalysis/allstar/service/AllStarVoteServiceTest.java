package com.example.mlbanalysis.allstar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.mlbanalysis.allstar.repository.AllStarBallotRepository;
import com.example.mlbanalysis.allstar.repository.AllStarResultRow;
import com.example.mlbanalysis.auth.service.AuthService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AllStarVoteServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-09T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AllStarBallotRepository ballotRepository;

    @Mock
    private AuthService authService;

    @Test
    void publicResultsAggregateSelectionsWithoutRequiringLogin() {
        AllStarVoteService service = new AllStarVoteService(ballotRepository, authService, clock);
        when(ballotRepository.countByVoteDate(LocalDate.parse("2026-06-09"))).thenReturn(2L);
        when(ballotRepository.summarizeResultsByVoteDate(LocalDate.parse("2026-06-09"))).thenReturn(List.of(
                row("P", 660271, "Shohei Ohtani", "Los Angeles Dodgers", 2L),
                row("C", 543877, "Salvador Perez", "Kansas City Royals", 1L),
                row("C", 592663, "Will Smith", "Los Angeles Dodgers", 1L)
        ));

        var results = service.publicResults();

        assertThat(results.voteDate()).isEqualTo(LocalDate.parse("2026-06-09"));
        assertThat(results.totalBallots()).isEqualTo(2L);
        assertThat(results.positions()).hasSize(2);
        assertThat(results.positions().getFirst().positionKey()).isEqualTo("C");
        assertThat(results.positions().getFirst().candidates().getFirst().votePercentage()).isEqualTo(50);
        assertThat(results.positions().get(1).positionKey()).isEqualTo("P");
        assertThat(results.positions().get(1).candidates().getFirst().playerName()).isEqualTo("Shohei Ohtani");
        assertThat(results.positions().get(1).candidates().getFirst().voteCount()).isEqualTo(2L);
        assertThat(results.positions().get(1).candidates().getFirst().votePercentage()).isEqualTo(100);
        verifyNoInteractions(authService);
    }

    @Test
    void publicResultsHandlesNoBallots() {
        AllStarVoteService service = new AllStarVoteService(ballotRepository, authService, clock);
        when(ballotRepository.countByVoteDate(LocalDate.parse("2026-06-09"))).thenReturn(0L);
        when(ballotRepository.summarizeResultsByVoteDate(LocalDate.parse("2026-06-09"))).thenReturn(List.of());

        var results = service.publicResults();

        assertThat(results.totalBallots()).isZero();
        assertThat(results.positions()).isEmpty();
        verifyNoInteractions(authService);
    }

    private AllStarResultRow row(String positionKey, Integer playerId, String playerName, String teamName, Long voteCount) {
        return new AllStarResultRow() {
            @Override
            public String getPositionKey() {
                return positionKey;
            }

            @Override
            public Integer getPlayerId() {
                return playerId;
            }

            @Override
            public String getPlayerName() {
                return playerName;
            }

            @Override
            public String getTeamName() {
                return teamName;
            }

            @Override
            public Long getVoteCount() {
                return voteCount;
            }
        };
    }
}
