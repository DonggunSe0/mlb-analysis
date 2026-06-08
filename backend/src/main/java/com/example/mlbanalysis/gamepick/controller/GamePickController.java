package com.example.mlbanalysis.gamepick.controller;

import com.example.mlbanalysis.gamepick.dto.GamePickListResponse;
import com.example.mlbanalysis.gamepick.dto.GamePickRequest;
import com.example.mlbanalysis.gamepick.dto.GamePickResponse;
import com.example.mlbanalysis.gamepick.dto.GamePickSummaryResponse;
import com.example.mlbanalysis.gamepick.service.GamePickSummaryService;
import com.example.mlbanalysis.gamepick.service.GamePickService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@Profile("login")
public class GamePickController {
    private final GamePickService gamePickService;
    private final GamePickSummaryService gamePickSummaryService;

    public GamePickController(GamePickService gamePickService, GamePickSummaryService gamePickSummaryService) {
        this.gamePickService = gamePickService;
        this.gamePickSummaryService = gamePickSummaryService;
    }

    @Operation(summary = "Return the logged-in user's pick for a game")
    @GetMapping("/{gamePk}/pick")
    public ResponseEntity<GamePickResponse> pick(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long gamePk
    ) {
        GamePickResponse response = gamePickService.pick(authorizationHeader, gamePk);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Create or update the logged-in user's pick for a game")
    @PostMapping("/{gamePk}/pick")
    public GamePickResponse submit(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long gamePk,
            @Valid @RequestBody GamePickRequest request
    ) {
        return gamePickService.submit(authorizationHeader, gamePk, request);
    }

    @Operation(summary = "Return the logged-in user's game picks")
    @GetMapping("/picks/me")
    public GamePickListResponse myPicks(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return gamePickService.myPicks(authorizationHeader);
    }

    @Operation(summary = "Return fan pick consensus for a game")
    @GetMapping("/{gamePk}/pick-summary")
    public GamePickSummaryResponse summary(@PathVariable Long gamePk) {
        return gamePickSummaryService.summary(gamePk);
    }
}
