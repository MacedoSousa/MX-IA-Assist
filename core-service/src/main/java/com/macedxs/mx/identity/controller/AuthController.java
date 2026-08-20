package com.macedxs.mx.identity.controller;

import com.macedxs.mx.identity.dto.LoginRequest;
import com.macedxs.mx.identity.dto.LoginResponse;
import com.macedxs.mx.identity.dto.RefreshRequest;
import com.macedxs.mx.identity.dto.RefreshResponse;
import com.macedxs.mx.identity.dto.RegisterRequest;
import com.macedxs.mx.identity.dto.RegisterResponse;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserSession;
import com.macedxs.mx.identity.service.AuthService;
import com.macedxs.mx.identity.service.JwtService;
import com.macedxs.mx.identity.service.RefreshTokenService;
import com.macedxs.mx.identity.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${auth.refresh-expiration-ms:2592000000}")
    private long refreshExpirationMs;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          SessionService sessionService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        UserEntity user = authService.register(request.name(), request.email(), request.password());

        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = authService.authenticate(request.email(), request.password());
        String deviceName = request.deviceName() == null || request.deviceName().isBlank()
                ? "unknown"
                : request.deviceName().trim();
        UserSession session = sessionService.createSession(user, deviceName);
        RefreshTokenService.IssuedRefreshToken issuedRefreshToken = refreshTokenService.issue(session);
        sessionService.save(session);
        String token = jwtService.generateToken(user, session.getId());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                token,
                jwtExpirationMs,
                session.getId(),
                issuedRefreshToken.value()
        );
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
        SessionService.RotatedRefreshToken rotated = sessionService
                .rotateRefreshToken(request.refreshToken(), refreshTokenService)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        String token = jwtService.generateToken(
                rotated.userId(),
                rotated.email(),
                rotated.role(),
                rotated.sessionId()
        );

        return new RefreshResponse(
                rotated.userId(),
                rotated.email(),
                rotated.name(),
                rotated.role(),
                token,
                jwtExpirationMs,
                rotated.sessionId(),
                rotated.refreshToken(),
                refreshExpirationMs
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token == null || !jwtService.validateToken(token)) {
            return;
        }

        try {
            UUID userId = jwtService.extractUserId(token);
            UUID sessionId = jwtService.extractSessionId(token);
            sessionService.revoke(sessionId, userId);
        } catch (RuntimeException ignored) {
            // Logout é idempotente: tokens inválidos não revelam detalhes internos.
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }
}
