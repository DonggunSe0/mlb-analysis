package com.example.mlbanalysis.gamepick.service;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.gamepick.dto.GamePickListResponse;
import com.example.mlbanalysis.gamepick.dto.GamePickRequest;
import com.example.mlbanalysis.gamepick.dto.GamePickResponse;
import com.example.mlbanalysis.gamepick.entity.GamePick;
import com.example.mlbanalysis.gamepick.repository.GamePickRepository;
import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class GamePickService {
    private final GamePickRepository gamePickRepository;
    private final AuthService authService;
    private final Clock clock;

    public GamePickService(GamePickRepository gamePickRepository, AuthService authService, Clock clock) {
        this.gamePickRepository = gamePickRepository;
        this.authService = authService;
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
        GamePick pick = gamePickRepository.findByUserAndGamePk(user, gamePk)
                .orElseGet(() -> new GamePick(user, gamePk, request.pickedTeamId(), request.pickedTeamName().trim(), clock.instant()));
        pick.updatePick(request.pickedTeamId(), request.pickedTeamName().trim());
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
}
