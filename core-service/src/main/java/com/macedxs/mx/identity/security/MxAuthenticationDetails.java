package com.macedxs.mx.identity.security;

import java.util.UUID;

public record MxAuthenticationDetails(UUID userId, UUID sessionId) {
}
