package com.example.mlbanalysis.gamepick.service;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.game.dto.GameResponse;
import com.example.mlbanalysis.game.service.GameService;
import com.example.mlbanalysis.gamepick.dto.GamePickListResponse;
import com.example.mlbanalysis.gamepick.dto.GamePickRequest;
import com.example.mlbanalysis.gamepick.dto.GamePickResponse;
import com.example.mlbanalysis.gamepick.entity.GamePick;
import com.example.mlbanalysis.gamepick.repository.GamePickRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class GamePickService {
    private final GamePickRepository gamePickRepository;
    private final AuthService authService;
    private final GameService gameService;
    private final Clock clock;

    public GamePickService(GamePickRepository gamePickRepository, AuthService authService, GameService gameService, Clock clock) {
        this.gamePickRepository = gamePickRepository;
        this.authService = authService;
        this.gameService = gameService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public GamePickResponse pick(String authorizationHeader, Long gamePk) {
        AuthUser user = authService.requireUser(authorizationHeader);
        return gamePickRepository.findByUserAndGamePk(user, gamePk)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public GamePickResponse submit(String authorizationHeader, Long gamePk, GamePickRequest request) {
        AuthUser user = authService.requireUser(authorizationHeader);
        TeamChoice choice = requireTeamChoice(gamePk, request);
        GamePick pick = gamePickRepository.findByUserAndGamePk(user, gamePk)
                .orElseGet(() -> new GamePick(user, gamePk, choice.teamId(), choice.teamName(), clock.instant()));
        pick.updatePick(choice.teamId(), choice.teamName());
        return toResponse(gamePickRepository.save(pick));
    }

    @Transactional(readOnly = true)
    public GamePickListResponse myPicks(String authorizationHeader) {
        AuthUser user = authService.requireUser(authorizationHeader);
        return new GamePickListResponse(gamePickRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList());
    }

    private GamePickResponse toResponse(GamePick pick) {
        return new GamePickResponse(pick.getGamePk(), pick.getPickedTeamId(), pick.getPickedTeamName(), pick.getCreatedAt());
    }

    private TeamChoice requireTeamChoice(Long gamePk, GamePickRequest request) {
        LocalDate date = parseGameDate(request.gameDate());
        GameResponse game = gameService.getGames(date).games().stream()
                .filter(candidate -> gamePk.equals(candidate.gamePk()))
                .findFirst()
                .orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, "GAME_PICK_GAME_NOT_FOUND", "Selected game is not available."));
        if (matchesTeam(request.pickedTeamId(), game.homeTeamId())) {
            return new TeamChoice(request.pickedTeamId(), requireTeamName(game.homeTeam()));
        }
        if (matchesTeam(request.pickedTeamId(), game.awayTeamId())) {
            return new TeamChoice(request.pickedTeamId(), requireTeamName(game.awayTeam()));
        }
        throw new AuthException(HttpStatus.BAD_REQUEST, "GAME_PICK_INVALID_TEAM", "Selected team is not part of this game.");
    }

    private LocalDate parseGameDate(String gameDate) {
        try {
            return LocalDate.parse(gameDate.trim());
        } catch (DateTimeParseException exception) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "GAME_PICK_INVALID_DATE", "Game date is invalid.");
        }
    }

    private boolean matchesTeam(Long pickedTeamId, Integer gameTeamId) {
        return gameTeamId != null && pickedTeamId.equals(gameTeamId.longValue());
    }

    private String requireTeamName(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "GAME_PICK_GAME_NOT_FOUND", "Selected game is not available.");
        }
        return teamName.trim();
    }

    private record TeamChoice(Long teamId, String teamName) {
    }
}
