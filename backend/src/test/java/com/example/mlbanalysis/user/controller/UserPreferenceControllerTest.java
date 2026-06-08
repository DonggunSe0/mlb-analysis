package com.example.mlbanalysis.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mlbanalysis.common.error.ApiExceptionHandler;
import com.example.mlbanalysis.user.dto.UserPreferenceRequest;
import com.example.mlbanalysis.user.dto.UserPreferenceResponse;
import com.example.mlbanalysis.user.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserPreferenceController.class)
@Import(ApiExceptionHandler.class)
@ActiveProfiles("login")
class UserPreferenceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserPreferenceService userPreferenceService;

    @Test
    void getPreferencesReturnsFavoriteTeam() throws Exception {
        when(userPreferenceService.preferences("Bearer token"))
                .thenReturn(new UserPreferenceResponse(111L, "Los Angeles Dodgers"));

        mockMvc.perform(get("/api/v1/users/me/preferences").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoriteTeamId").value(111))
                .andExpect(jsonPath("$.favoriteTeamName").value("Los Angeles Dodgers"));
    }

    @Test
    void putPreferencesUpdatesFavoriteTeam() throws Exception {
        when(userPreferenceService.update(eq("Bearer token"), any(UserPreferenceRequest.class)))
                .thenReturn(new UserPreferenceResponse(147L, "New York Yankees"));

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"favoriteTeamId\":147}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoriteTeamId").value(147))
                .andExpect(jsonPath("$.favoriteTeamName").value("New York Yankees"));
    }
}
