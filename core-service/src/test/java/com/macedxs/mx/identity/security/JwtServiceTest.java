package com.macedxs.mx.identity.security;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserSession;
import com.macedxs.mx.identity.service.JwtService;
import com.macedxs.mx.identity.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionService sessionService;

    @Test
    void shouldGenerateAndValidateJwt() {
        UserEntity user = new UserEntity();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hashed");
        user.setRole("USER");
        user.setActive(true);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void shouldCreateValidSessionAndRevocateIt() {
        UserEntity user = new UserEntity();
        user.setName("Bob");
        user.setEmail("bob@example.com");
        user.setPasswordHash("hashed");
        user.setRole("ADMIN");
        user.setActive(true);

        UserSession session = sessionService.createSession(user, "device-1");

        assertThat(session.getId()).isNotNull();
        assertThat(sessionService.isValid(session)).isTrue();

        sessionService.revoke(session);
        assertThat(sessionService.isValid(session)).isFalse();
    }
}
