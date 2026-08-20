package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserSession;
import com.macedxs.mx.identity.repository.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Test
    void revokeAllActiveSessions_shouldInvalidateEverySessionForUser() {
        UserEntity user = authService.register("Alice", "alice@example.com", "Strong123!");

        UserSession first = sessionService.createSession(user, "desktop");
        UserSession second = sessionService.createSession(user, "mobile");

        sessionService.revokeAllActiveSessions(user);

        UserSession refreshedFirst = userSessionRepository.findById(first.getId()).orElseThrow();
        UserSession refreshedSecond = userSessionRepository.findById(second.getId()).orElseThrow();

        assertThat(userSessionRepository.findByUserIdAndActiveTrue(user.getId())).isEmpty();
        assertThat(sessionService.isValid(refreshedFirst)).isFalse();
        assertThat(sessionService.isValid(refreshedSecond)).isFalse();
    }
}
