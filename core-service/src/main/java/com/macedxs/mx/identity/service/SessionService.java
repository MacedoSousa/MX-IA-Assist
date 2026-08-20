package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserSession;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.identity.repository.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    public SessionService(UserRepository userRepository, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public UserSession createSession(UserEntity user, String deviceName) {

        if (user == null) {
            throw new IllegalArgumentException("User is required to create a session");
        }

        if (user.getId() == null) {
            user = userRepository.save(user);
        }

        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceName(deviceName);
        session.setActive(true);
        return userSessionRepository.save(session);
    }

    @Transactional
    public UserSession save(UserSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session is required");
        }
        return userSessionRepository.save(session);
    }

    @Transactional
    public Optional<RotatedRefreshToken> rotateRefreshToken(String rawRefreshToken,
                                                              RefreshTokenService refreshTokenService) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return Optional.empty();
        }

        UserSession session;
        try {
            session = userSessionRepository
                    .findByRefreshTokenHash(refreshTokenService.hash(rawRefreshToken))
                    .orElse(null);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }

        if (session == null) {
            return Optional.empty();
        }
        if (!refreshTokenService.matches(session, rawRefreshToken)) {
            revoke(session);
            return Optional.empty();
        }

        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(session);
        userSessionRepository.saveAndFlush(session);
        UserEntity user = session.getUser();
        return Optional.of(new RotatedRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                session.getId(),
                issued.value(),
                issued.expiresAt()
        ));
    }

    @Transactional
    public void revoke(UserSession session) {

        if (session == null) {
            return;
        }
        session.setActive(false);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.saveAndFlush(session);
    }

    @Transactional
    public void revokeAllActiveSessions(UserEntity user) {
        if (user == null || user.getId() == null) {
            return;
        }

        List<UserSession> activeSessions = userSessionRepository.findByUserIdAndActiveTrue(user.getId());
        for (UserSession session : activeSessions) {
            revoke(session);
        }
    }

    public Optional<UserSession> findByIdAndUserId(UUID sessionId, UUID userId) {
        if (sessionId == null || userId == null) {
            return Optional.empty();
        }
        return userSessionRepository.findByIdAndUserId(sessionId, userId);
    }

    @Transactional
    public boolean revoke(UUID sessionId, UUID userId) {
        return findByIdAndUserId(sessionId, userId)
                .map(session -> {
                    revoke(session);
                    return true;
                })
                .orElse(false);
    }

    public boolean isValid(UUID sessionId, UUID userId) {
        return findByIdAndUserId(sessionId, userId)
                .map(this::isValid)
                .orElse(false);
    }

    public boolean isValid(UserSession session) {

        return session != null
                && session.isActive()
                && session.getRevokedAt() == null
                && session.getExpiresAt() != null
                && session.getExpiresAt().isAfter(LocalDateTime.now());
    }

    public List<UserSession> findActiveSessionsForUser(UUID userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    public record RotatedRefreshToken(
            UUID userId,
            String email,
            String name,
            String role,
            UUID sessionId,
            String refreshToken,
            LocalDateTime refreshExpiresAt
    ) {
    }
}
