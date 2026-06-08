package com.example.mlbanalysis.game.controller;

import com.example.mlbanalysis.game.dto.GameListResponse;
import com.example.mlbanalysis.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Return MLB games for a date from the external MLB Stats API")
    @GetMapping
    public GameListResponse getGames(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return gameService.getGames(date);
    }
}
