package com.example.mlbanalysis.user.service;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.user.dto.UserPreferenceRequest;
import com.example.mlbanalysis.user.dto.UserPreferenceResponse;
import com.example.mlbanalysis.user.entity.UserPreference;
import com.example.mlbanalysis.user.repository.UserPreferenceRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class UserPreferenceService {
    private final UserPreferenceRepository preferenceRepository;
    private final AuthService authService;

    public UserPreferenceService(UserPreferenceRepository preferenceRepository, AuthService authService) {
        this.preferenceRepository = preferenceRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse preferences(String authorizationHeader) {
        AuthUser user = authService.requireUser(authorizationHeader);
        return preferenceRepository.findByUser(user)
                .map(this::toResponse)
                .orElseGet(() -> new UserPreferenceResponse(null, null));
    }

    @Transactional
    public UserPreferenceResponse update(String authorizationHeader, UserPreferenceRequest request) {
        AuthUser user = authService.requireUser(authorizationHeader);
        UserPreference preference = preferenceRepository.findByUser(user)
                .orElseGet(() -> new UserPreference(user, null, null));
        preference.updateFavoriteTeam(request.favoriteTeamId(), request.favoriteTeamName());
        return toResponse(preferenceRepository.save(preference));
    }

    private UserPreferenceResponse toResponse(UserPreference preference) {
        return new UserPreferenceResponse(preference.getFavoriteTeamId(), preference.getFavoriteTeamName());
    }
}
