package com.example.mlbanalysis.auth.controller;

import com.example.mlbanalysis.auth.dto.AuthRequest;
import com.example.mlbanalysis.auth.dto.AuthResponse;
import com.example.mlbanalysis.auth.dto.CurrentUserResponse;
import com.example.mlbanalysis.auth.dto.RegisterRequest;
import com.example.mlbanalysis.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Profile("login")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @Operation(summary = "Create a login account backed by the application MySQL database")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) { return authService.login(request); }

    @Operation(summary = "Return the currently logged-in user")
    @GetMapping("/me")
    public CurrentUserResponse me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return authService.currentUser(authorizationHeader);
    }

    @Operation(summary = "Logout the current token")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        authService.logout(authorizationHeader);
    }
}
