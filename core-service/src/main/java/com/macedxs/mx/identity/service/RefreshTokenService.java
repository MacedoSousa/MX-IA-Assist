package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 48;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.refresh-expiration-ms:2592000000}")
    private long expirationMs;

    public IssuedRefreshToken issue(UserSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session is required to issue a refresh token");
        }
        if (!session.isActive()) {
            throw new IllegalArgumentException("Cannot issue a refresh token for an inactive session");
        }

        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(expirationMs));

        session.setRefreshTokenHash(hash(value));
        session.setRefreshExpiresAt(expiresAt);
        return new IssuedRefreshToken(value, expiresAt);
    }

    public boolean matches(UserSession session, String rawToken) {
        if (session == null
                || rawToken == null
                || rawToken.isBlank()
                || !session.isActive()
                || session.getRevokedAt() != null
                || session.getRefreshTokenHash() == null
                || session.getRefreshExpiresAt() == null
                || !session.getRefreshExpiresAt().isAfter(LocalDateTime.now())) {
            return false;
        }

        return MessageDigest.isEqual(
                session.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8),
                hash(rawToken).getBytes(StandardCharsets.UTF_8)
        );
    }

    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public record IssuedRefreshToken(String value, LocalDateTime expiresAt) {
    }
}
