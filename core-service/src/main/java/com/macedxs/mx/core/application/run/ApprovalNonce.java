package com.macedxs.mx.core.application.run;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class ApprovalNonce {

    private ApprovalNonce() {
    }

    public static Issued issue() {
        String raw = UUID.randomUUID().toString() + UUID.randomUUID();
        return new Issued(raw, hash(raw));
    }

    public static String hash(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Approval nonce is required");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static boolean matches(String raw, String expectedHash) {
        if (raw == null || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(raw).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    public record Issued(String raw, String hash) {
        public Issued {
            if (raw == null || raw.isBlank() || hash == null || hash.isBlank()) {
                throw new IllegalArgumentException("Issued approval nonce is invalid");
            }
        }
    }
}
