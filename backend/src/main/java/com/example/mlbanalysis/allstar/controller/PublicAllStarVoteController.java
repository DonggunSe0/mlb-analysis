package com.example.mlbanalysis.allstar.controller;

import com.example.mlbanalysis.allstar.dto.AllStarVoteResultsResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/all-star/votes")
@Profile("!login")
public class PublicAllStarVoteController {
    @Operation(summary = "Return public All-Star voting results without login persistence")
    @GetMapping("/results")
    public AllStarVoteResultsResponse results() {
        return new AllStarVoteResultsResponse(LocalDate.now(), 0L, List.of());
    }
}
