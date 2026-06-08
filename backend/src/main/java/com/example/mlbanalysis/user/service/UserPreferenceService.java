package com.example.mlbanalysis.user.service;

import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.service.AuthException;
import com.example.mlbanalysis.auth.service.AuthService;
import com.example.mlbanalysis.team.dto.TeamResponse;
import com.example.mlbanalysis.team.service.TeamService;
import com.example.mlbanalysis.user.dto.UserPreferenceRequest;
import com.example.mlbanalysis.user.dto.UserPreferenceResponse;
import com.example.mlbanalysis.user.entity.UserPreference;
import com.example.mlbanalysis.user.repository.UserPreferenceRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("login")
public class UserPreferenceService {
    private final UserPreferenceRepository preferenceRepository;
    private final AuthService authService;
    private final TeamService teamService;

    public UserPreferenceService(UserPreferenceRepository preferenceRepository, AuthService authService, TeamService teamService) {
        this.preferenceRepository = preferenceRepository;
        this.authService = authService;
        this.teamService = teamService;
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
        TeamResponse team = requireTeam(request.favoriteTeamId());
        UserPreference preference = preferenceRepository.findByUser(user)
                .orElseGet(() -> new UserPreference(user, null, null));
        preference.updateFavoriteTeam(request.favoriteTeamId(), team.name());
        return toResponse(preferenceRepository.save(preference));
    }

    private UserPreferenceResponse toResponse(UserPreference preference) {
        return new UserPreferenceResponse(preference.getFavoriteTeamId(), preference.getFavoriteTeamName());
    }

    private TeamResponse requireTeam(Long favoriteTeamId) {
        return teamService.getTeams().teams().stream()
                .filter(team -> team.id() != null && favoriteTeamId.equals(team.id().longValue()))
                .findFirst()
                .orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, "USER_PREFERENCE_INVALID_TEAM", "Selected team is not available."));
    }
}
