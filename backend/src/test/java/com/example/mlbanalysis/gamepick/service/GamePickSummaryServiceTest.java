package com.example.mlbanalysis.gamepick.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.mlbanalysis.gamepick.repository.GamePickSummaryRow;
import com.example.mlbanalysis.gamepick.repository.GamePickRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GamePickSummaryServiceTest {
    @Mock
    private GamePickRepository gamePickRepository;

    private GamePickSummaryService gamePickSummaryService;

    @BeforeEach
    void setUp() {
        gamePickSummaryService = new GamePickSummaryService(gamePickRepository);
    }

    @Test
    void summaryReturnsConsensusPercentagesForGame() {
        when(gamePickRepository.summarizeByGamePk(778899L)).thenReturn(List.of(
                new SummaryRow(111L, "Los Angeles Dodgers", 3L),
                new SummaryRow(147L, "New York Yankees", 1L)
        ));

        var response = gamePickSummaryService.summary(778899L);

        assertThat(response.gamePk()).isEqualTo(778899L);
        assertThat(response.totalPicks()).isEqualTo(4L);
        assertThat(response.teams()).hasSize(2);
        assertThat(response.teams().getFirst().pickPercentage()).isEqualTo(75);
        assertThat(response.teams().getFirst().leading()).isTrue();
        assertThat(response.teams().get(1).pickPercentage()).isEqualTo(25);
        assertThat(response.teams().get(1).leading()).isFalse();
    }

    @Test
    void summaryHandlesGamesWithoutPicks() {
        when(gamePickRepository.summarizeByGamePk(778899L)).thenReturn(List.of());

        var response = gamePickSummaryService.summary(778899L);

        assertThat(response.totalPicks()).isZero();
        assertThat(response.teams()).isEmpty();
    }

    private record SummaryRow(Long pickedTeamId, String pickedTeamName, Long pickCount) implements GamePickSummaryRow {
        @Override
        public Long getPickedTeamId() {
            return pickedTeamId;
        }

        @Override
        public String getPickedTeamName() {
            return pickedTeamName;
        }

        @Override
        public Long getPickCount() {
            return pickCount;
        }
    }
}
