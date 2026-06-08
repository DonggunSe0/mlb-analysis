package com.example.mlbanalysis.allstar.service;

import com.example.mlbanalysis.allstar.dto.AllStarBallotRequest;
import com.example.mlbanalysis.allstar.dto.AllStarBallotResponse;
import com.example.mlbanalysis.allstar.dto.AllStarSelectionRequest;
import com.example.mlbanalysis.allstar.dto.AllStarSelectionResponse;
import com.example.mlbanalysis.allstar.dto.AllStarVoteStatusResponse;
import com.example.mlbanalysis.allstar.entity.AllStarBallot;
import com.example.mlbanalysis.allstar.entity.AllStarSelection;
import com.example.mlbanalysis.allstar.repository.AllStarBallotRepository;
import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.auth.service.AuthService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
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
}
