package com.example.mlbanalysis.gamepick.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.game.dto.GameListResponse;
import com.example.mlbanalysis.game.dto.GameResponse;
import com.example.mlbanalysis.game.service.GameService;
import com.example.mlbanalysis.gamepick.dto.GamePickRequest;
import com.example.mlbanalysis.gamepick.entity.GamePick;
import com.example.mlbanalysis.gamepick.repository.GamePickRepository;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GamePickServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-09T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private GamePickRepository gamePickRepository;

    @Mock
    private AuthService authService;

    @Mock
    private GameService gameService;

    private GamePickService gamePickService;

    @BeforeEach
    void setUp() {
        gamePickService = new GamePickService(gamePickRepository, authService, gameService, CLOCK);
    }

    @Test
    void submitCreatesPickForAuthenticatedUser() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(gameService.getGames(LocalDate.parse("2026-06-09"))).thenReturn(gamesWithDodgersYankees());
        when(gamePickRepository.findByUserAndGamePk(user, 778899L)).thenReturn(Optional.empty());
        when(gamePickRepository.save(any(GamePick.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = gamePickService.submit("Bearer token", 778899L, new GamePickRequest("2026-06-09", 111L));

        assertThat(response.gamePk()).isEqualTo(778899L);
        assertThat(response.pickedTeamId()).isEqualTo(111L);
        assertThat(response.pickedTeamName()).isEqualTo("Los Angeles Dodgers");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-06-09T12:00:00Z"));

        ArgumentCaptor<GamePick> captor = ArgumentCaptor.forClass(GamePick.class);
        verify(gamePickRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void submitUpdatesExistingPickForSameUserAndGame() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        GamePick existing = new GamePick(user, 778899L, 111L, "Los Angeles Dodgers", Instant.parse("2026-06-09T10:00:00Z"));
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(gameService.getGames(LocalDate.parse("2026-06-09"))).thenReturn(gamesWithDodgersYankees());
        when(gamePickRepository.findByUserAndGamePk(user, 778899L)).thenReturn(Optional.of(existing));
        when(gamePickRepository.save(existing)).thenReturn(existing);

        var response = gamePickService.submit("Bearer token", 778899L, new GamePickRequest("2026-06-09", 147L));

        assertThat(response.pickedTeamId()).isEqualTo(147L);
        assertThat(response.pickedTeamName()).isEqualTo("New York Yankees");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-06-09T10:00:00Z"));
    }

    @Test
    void myPicksReturnsNewestFirst() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        GamePick first = new GamePick(user, 10L, 111L, "Los Angeles Dodgers", Instant.parse("2026-06-09T10:00:00Z"));
        GamePick second = new GamePick(user, 11L, 147L, "New York Yankees", Instant.parse("2026-06-09T11:00:00Z"));
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(gamePickRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(java.util.List.of(second, first));

        var response = gamePickService.myPicks("Bearer token");

        assertThat(response.picks()).extracting("gamePk").containsExactly(11L, 10L);
    }

    @Test
    void submitRejectsTeamThatIsNotInGame() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(gameService.getGames(LocalDate.parse("2026-06-09"))).thenReturn(gamesWithDodgersYankees());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        gamePickService.submit("Bearer token", 778899L, new GamePickRequest("2026-06-09", 999L)))
                .isInstanceOf(AuthException.class)
                .hasMessage("Selected team is not part of this game.");
    }

    private GameListResponse gamesWithDodgersYankees() {
        return new GameListResponse(java.util.List.of(new GameResponse(
                778899L,
                "2026-06-09T19:05:00Z",
                "Scheduled",
                147,
                "New York Yankees",
                111,
                "Los Angeles Dodgers",
                null,
                null
        )));
    }
}
