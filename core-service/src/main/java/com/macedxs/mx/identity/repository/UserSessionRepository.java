package com.macedxs.mx.identity.repository;

import com.macedxs.mx.identity.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
        List<UserSession> findByUserIdAndActiveTrue(UUID userId);

    Optional<UserSession> findByIdAndUserId(UUID id, UUID userId);

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

}
