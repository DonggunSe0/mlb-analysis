package com.example.mlbanalysis.allstar.service;

import com.example.mlbanalysis.allstar.dto.AllStarBallotRequest;
import com.example.mlbanalysis.allstar.dto.AllStarBallotResponse;
import com.example.mlbanalysis.allstar.dto.AllStarResultCandidateResponse;
import com.example.mlbanalysis.allstar.dto.AllStarResultPositionResponse;
import com.example.mlbanalysis.allstar.dto.AllStarSelectionRequest;
import com.example.mlbanalysis.allstar.dto.AllStarSelectionResponse;
import com.example.mlbanalysis.allstar.dto.AllStarVoteResultsResponse;
import com.example.mlbanalysis.allstar.dto.AllStarVoteStatusResponse;
import com.example.mlbanalysis.allstar.entity.AllStarBallot;
import com.example.mlbanalysis.allstar.entity.AllStarSelection;
import com.example.mlbanalysis.allstar.repository.AllStarBallotRepository;
import com.example.mlbanalysis.allstar.repository.AllStarResultRow;
import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.auth.service.AuthService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class AllStarVoteService {
    private final AllStarBallotRepository ballotRepository;
    private final AuthService authService;
    private final Clock clock;

    public AllStarVoteService(AllStarBallotRepository ballotRepository, AuthService authService, Clock clock) {
        this.ballotRepository = ballotRepository;
        this.authService = authService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AllStarVoteStatusResponse status(String authorizationHeader) {
        AuthUser user = authService.requireUser(authorizationHeader);
        LocalDate today = LocalDate.now(clock);
        AllStarBallotResponse ballot = ballotRepository.findByUserIdAndVoteDate(user.getId(), today)
                .map(this::toResponse)
                .orElse(null);
        return new AllStarVoteStatusResponse(ballot == null, today, ballot);
    }

    @Transactional(readOnly = true)
    public AllStarVoteResultsResponse publicResults() {
        LocalDate today = LocalDate.now(clock);
        long totalBallots = ballotRepository.countByVoteDate(today);
        Map<String, Map<CandidateKey, Long>> countsByPosition = new LinkedHashMap<>();
        for (AllStarResultRow row : ballotRepository.summarizeResultsByVoteDate(today)) {
            countsByPosition
                    .computeIfAbsent(row.getPositionKey(), ignored -> new LinkedHashMap<>())
                    .put(new CandidateKey(row.getPlayerId(), row.getPlayerName(), row.getTeamName()), row.getVoteCount());
        }

        List<AllStarResultPositionResponse> positions = countsByPosition.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toPositionResult(entry.getKey(), entry.getValue()))
                .toList();
        return new AllStarVoteResultsResponse(today, totalBallots, positions);
    }

    @Transactional
    public AllStarBallotResponse vote(String authorizationHeader, AllStarBallotRequest request) {
        AuthUser user = authService.requireUser(authorizationHeader);
        LocalDate today = LocalDate.now(clock);
        if (ballotRepository.existsByUserIdAndVoteDate(user.getId(), today)) {
            throw new AuthException(HttpStatus.CONFLICT, "ALL_STAR_ALREADY_VOTED", "All-Star voting is allowed once per day.");
        }
        validateUniquePositions(request);
        AllStarBallot ballot = new AllStarBallot(user, today, clock.instant());
        for (AllStarSelectionRequest selection : request.selections()) {
            ballot.addSelection(new AllStarSelection(
                    selection.positionKey().trim(),
                    selection.playerId(),
                    selection.playerName().trim(),
                    selection.teamName() == null ? null : selection.teamName().trim()
            ));
        }
        return toResponse(ballotRepository.save(ballot));
    }

    private void validateUniquePositions(AllStarBallotRequest request) {
        HashSet<String> positions = new HashSet<>();
        for (AllStarSelectionRequest selection : request.selections()) {
            if (!positions.add(selection.positionKey().trim())) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "ALL_STAR_DUPLICATE_POSITION", "Each formation position can be selected once.");
            }
        }
    }

    private AllStarBallotResponse toResponse(AllStarBallot ballot) {
        return new AllStarBallotResponse(
                ballot.getId(),
                ballot.getVoteDate(),
                ballot.getCreatedAt(),
                ballot.getSelections().stream()
                        .map(selection -> new AllStarSelectionResponse(
                                selection.getPositionKey(),
                                selection.getPlayerId(),
                                selection.getPlayerName(),
                                selection.getTeamName()
                        ))
                        .toList()
        );
    }

    private AllStarResultPositionResponse toPositionResult(String positionKey, Map<CandidateKey, Long> voteCounts) {
        long totalVotes = voteCounts.values().stream().mapToLong(Long::longValue).sum();
        long leaderCount = voteCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        long leaderOccurrences = voteCounts.values().stream()
                .filter(count -> count == leaderCount)
                .count();
        List<AllStarResultCandidateResponse> candidates = voteCounts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<CandidateKey, Long>, Long>comparing(Map.Entry::getValue).reversed()
                        .thenComparing(entry -> entry.getKey().playerName()))
                .map(entry -> {
                    long voteCount = entry.getValue();
                    CandidateKey candidate = entry.getKey();
                    return new AllStarResultCandidateResponse(
                            candidate.playerId(),
                            candidate.playerName(),
                            candidate.teamName(),
                            voteCount,
                            totalVotes == 0 ? 0 : Math.round((voteCount * 100f) / totalVotes),
                            leaderCount > 0 && leaderOccurrences == 1 && voteCount == leaderCount
                    );
                })
                .toList();
        return new AllStarResultPositionResponse(positionKey, totalVotes, candidates);
    }

    private record CandidateKey(Integer playerId, String playerName, String teamName) {
    }
}
