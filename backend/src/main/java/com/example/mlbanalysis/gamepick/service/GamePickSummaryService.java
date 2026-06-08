package com.example.mlbanalysis.gamepick.service;

import com.example.mlbanalysis.gamepick.dto.GamePickSummaryResponse;
import com.example.mlbanalysis.gamepick.dto.GamePickTeamSummaryResponse;
import com.example.mlbanalysis.gamepick.repository.GamePickRepository;
import com.example.mlbanalysis.gamepick.repository.GamePickSummaryRow;
import java.util.Comparator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class GamePickSummaryService {
    private final GamePickRepository gamePickRepository;

    public GamePickSummaryService(GamePickRepository gamePickRepository) {
        this.gamePickRepository = gamePickRepository;
    }

    @Transactional(readOnly = true)
    public GamePickSummaryResponse summary(Long gamePk) {
        var rows = gamePickRepository.summarizeByGamePk(gamePk).stream()
                .sorted(Comparator.comparing(GamePickSummaryRow::getPickCount).reversed()
                        .thenComparing(GamePickSummaryRow::getPickedTeamName))
                .toList();
        long total = rows.stream().mapToLong(GamePickSummaryRow::getPickCount).sum();
        long leaderCount = rows.stream().mapToLong(GamePickSummaryRow::getPickCount).max().orElse(0);
        long leaderCountOccurrences = rows.stream()
                .filter(row -> row.getPickCount() == leaderCount)
                .count();

        var teams = rows.stream()
                .map(row -> new GamePickTeamSummaryResponse(
                        row.getPickedTeamId(),
                        row.getPickedTeamName(),
                        row.getPickCount(),
                        total == 0 ? 0 : Math.round((row.getPickCount() * 100f) / total),
                        leaderCount > 0 && leaderCountOccurrences == 1 && row.getPickCount() == leaderCount
                ))
                .toList();
        return new GamePickSummaryResponse(gamePk, total, teams);
    }
}
