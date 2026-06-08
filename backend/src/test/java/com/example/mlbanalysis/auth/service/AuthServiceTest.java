package com.example.mlbanalysis.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mlbanalysis.auth.dto.AuthRequest;
import com.example.mlbanalysis.auth.dto.RegisterRequest;
import com.example.mlbanalysis.auth.entity.AuthToken;
import com.example.mlbanalysis.auth.entity.AuthUser;
import com.example.mlbanalysis.auth.repository.AuthTokenRepository;
import com.example.mlbanalysis.auth.repository.AuthUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-09T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private AuthTokenRepository tokenRepository;

    @Test
    void registerNormalizesEmailHashesPasswordAndIssuesToken() {
        AuthService service = new AuthService(userRepository, tokenRepository, passwordHasher, clock);
        when(userRepository.existsByEmail("fan@example.com")).thenReturn(false);
        when(userRepository.save(any(AuthUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.register(new RegisterRequest(" Fan@Example.com ", " Fan ", "password123"));

        assertThat(response.token()).hasSize(64);
        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2026-06-16T00:00:00Z"));
        assertThat(response.user().email()).isEqualTo("fan@example.com");
        assertThat(response.user().displayName()).isEqualTo("Fan");

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).doesNotContain("password123");
        assertThat(passwordHasher.matches("password123", userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void loginRejectsInvalidPassword() {
        AuthService service = new AuthService(userRepository, tokenRepository, passwordHasher, clock);
        AuthUser user = new AuthUser("fan@example.com", "Fan", passwordHasher.hash("password123"));
        when(userRepository.findByEmail("fan@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new AuthRequest("fan@example.com", "wrong-password")))
                .isInstanceOf(AuthException.class)
                .extracting("status", "code")
                .containsExactly(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    @Test
    void currentUserRequiresValidBearerToken() {
        AuthService service = new AuthService(userRepository, tokenRepository, passwordHasher, clock);
        AuthUser user = new AuthUser("fan@example.com", "Fan", "hash");
        AuthToken token = new AuthToken("abc", user, Instant.parse("2026-06-10T00:00:00Z"));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        var currentUser = service.currentUser("Bearer abc");

        assertThat(currentUser.email()).isEqualTo("fan@example.com");
        assertThat(currentUser.displayName()).isEqualTo("Fan");
    }
}
