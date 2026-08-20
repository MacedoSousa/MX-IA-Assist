package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "mx-test-secret-key-with-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);

        user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRole("USER");
    }

    @Test
    void shouldGenerateTokenWithSessionAndUserClaims() {
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateToken(user, sessionId);

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
        assertThat(jwtService.extractSessionId(token)).isEqualTo(sessionId);
        assertThat(jwtService.extractEmail(token)).isEqualTo(user.getEmail());
    }

    @Test
    void shouldRejectSessionlessTokensForTheMultichannelFlow() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractSessionIdOrNull(token)).isNull();
    }
}
