package com.example.mlbanalysis.allstar.controller;

import com.example.mlbanalysis.allstar.dto.AllStarBallotRequest;
import com.example.mlbanalysis.allstar.dto.AllStarBallotResponse;
import com.example.mlbanalysis.allstar.dto.AllStarVoteStatusResponse;
import com.example.mlbanalysis.allstar.service.AllStarVoteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/all-star/votes")
@Profile("login")
public class AllStarVoteController {
    private final AllStarVoteService allStarVoteService;

    public AllStarVoteController(AllStarVoteService allStarVoteService) {
        this.allStarVoteService = allStarVoteService;
    }

    @Operation(summary = "Return today's All-Star vote status for the logged-in user")
    @GetMapping("/me")
    public AllStarVoteStatusResponse status(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return allStarVoteService.status(authorizationHeader);
    }

    @Operation(summary = "Submit today's All-Star ballot for the logged-in user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AllStarBallotResponse vote(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody AllStarBallotRequest request
    ) {
        return allStarVoteService.vote(authorizationHeader, request);
    }
}
