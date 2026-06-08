package com.example.mlbanalysis.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.user.dto.UserPreferenceRequest;
import com.example.mlbanalysis.user.entity.UserPreference;
import com.example.mlbanalysis.user.repository.UserPreferenceRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {
    @Mock
    private UserPreferenceRepository preferenceRepository;

    @Mock
    private AuthService authService;

    private UserPreferenceService userPreferenceService;

    @BeforeEach
    void setUp() {
        userPreferenceService = new UserPreferenceService(preferenceRepository, authService);
    }

    @Test
    void preferencesReturnsSavedFavoriteTeam() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(preferenceRepository.findByUser(user))
                .thenReturn(Optional.of(new UserPreference(user, 111L, "Los Angeles Dodgers")));

        var response = userPreferenceService.preferences("Bearer token");

        assertThat(response.favoriteTeamId()).isEqualTo(111L);
        assertThat(response.favoriteTeamName()).isEqualTo("Los Angeles Dodgers");
    }

    @Test
    void preferencesReturnsEmptyStateWhenNoFavoriteTeamExists() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.empty());

        var response = userPreferenceService.preferences("Bearer token");

        assertThat(response.favoriteTeamId()).isNull();
        assertThat(response.favoriteTeamName()).isNull();
    }

    @Test
    void updateCreatesOrReplacesFavoriteTeam() {
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        when(authService.requireUser("Bearer token")).thenReturn(user);
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userPreferenceService.update("Bearer token", new UserPreferenceRequest(147L, "New York Yankees"));

        assertThat(response.favoriteTeamId()).isEqualTo(147L);
        assertThat(response.favoriteTeamName()).isEqualTo("New York Yankees");
    }
}
