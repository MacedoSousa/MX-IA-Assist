package com.macedxs.mx.identity.service;

/**
 * Authentication failure deliberately kept generic to avoid account enumeration.
 */
public final class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
