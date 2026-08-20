package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret:mx-default-secret-key-for-local-development-please-change}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(UserEntity user) {
        return generateToken(user, null);
    }

    public String generateToken(UserEntity user, UUID sessionId) {
        if (user == null) {
            throw new IllegalArgumentException("User is required to generate a token");
        }
        return generateToken(user.getId(), user.getEmail(), user.getRole(), sessionId);
    }

    public String generateToken(UUID userId, String email, String role, UUID sessionId) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required to generate a token");
        }

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        var builder = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry));

        if (userId != null) {
            builder.claim("userId", userId.toString());
        }
        if (sessionId != null) {
            builder.claim("sessionId", sessionId.toString());
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).get("userId", String.class));
    }

    public UUID extractSessionId(String token) {
        String sessionId = extractClaims(token).get("sessionId", String.class);
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Token sessionId is required");
        }
        return UUID.fromString(sessionId);
    }

    public UUID extractSessionIdOrNull(String token) {
        try {
            String sessionId = extractClaims(token).get("sessionId", String.class);
            return sessionId == null || sessionId.isBlank() ? null : UUID.fromString(sessionId);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
