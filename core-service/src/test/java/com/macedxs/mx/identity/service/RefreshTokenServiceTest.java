package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenServiceTest {

    private RefreshTokenService refreshTokenService;
    private UserSession session;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService();
        ReflectionTestUtils.setField(refreshTokenService, "expirationMs", 3_600_000L);
        session = new UserSession();
        session.setDeviceName("test-device");
    }

    @Test
    void issue_shouldGenerateOpaqueTokenAndPersistOnlyItsHash() {
        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(session);

        assertThat(issued.value()).isNotBlank();
        assertThat(issued.value()).doesNotContain(session.getRefreshTokenHash());
        assertThat(session.getRefreshTokenHash()).isEqualTo(refreshTokenService.hash(issued.value()));
        assertThat(session.getRefreshExpiresAt()).isEqualTo(issued.expiresAt());
        assertThat(issued.expiresAt()).isAfter(LocalDateTime.now());
        assertThat(refreshTokenService.matches(session, issued.value())).isTrue();
    }

    @Test
    void matches_shouldRejectWrongExpiredAndRevokedTokens() {
        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(session);

        assertThat(refreshTokenService.matches(session, issued.value() + "tampered")).isFalse();

        session.setRefreshExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertThat(refreshTokenService.matches(session, issued.value())).isFalse();

        session.setRefreshExpiresAt(LocalDateTime.now().plusHours(1));
        session.setActive(false);
        assertThat(refreshTokenService.matches(session, issued.value())).isFalse();
    }

    @Test
    void issue_shouldRejectMissingSession() {
        assertThatThrownBy(() -> refreshTokenService.issue(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
