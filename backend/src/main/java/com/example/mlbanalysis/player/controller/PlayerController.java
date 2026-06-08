package com.example.mlbanalysis.player.controller;

import com.example.mlbanalysis.player.dto.PlayerResponse;
import com.example.mlbanalysis.player.dto.PlayerSearchResponse;
import com.example.mlbanalysis.player.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }


    @Operation(summary = "Search MLB players by name from the external MLB Stats API")
    @GetMapping("/search")
    public PlayerSearchResponse searchPlayers(@RequestParam String name) {
        return playerService.searchPlayers(name);
    }

    @Operation(summary = "Return an MLB player from the external MLB Stats API")
    @GetMapping("/{playerId}")
    public PlayerResponse getPlayer(@PathVariable Integer playerId) {
        return playerService.getPlayer(playerId);
    }
}
