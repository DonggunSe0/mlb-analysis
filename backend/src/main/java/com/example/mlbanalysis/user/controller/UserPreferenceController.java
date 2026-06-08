package com.example.mlbanalysis.user.controller;

import com.example.mlbanalysis.user.dto.UserPreferenceRequest;
import com.example.mlbanalysis.user.dto.UserPreferenceResponse;
import com.example.mlbanalysis.user.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/preferences")
@Profile("login")
public class UserPreferenceController {
    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @Operation(summary = "Return the logged-in user's personalization preferences")
    @GetMapping
    public UserPreferenceResponse preferences(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return userPreferenceService.preferences(authorizationHeader);
    }

    @Operation(summary = "Update the logged-in user's personalization preferences")
    @PutMapping
    public UserPreferenceResponse update(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody UserPreferenceRequest request
    ) {
        return userPreferenceService.update(authorizationHeader, request);
    }
}
