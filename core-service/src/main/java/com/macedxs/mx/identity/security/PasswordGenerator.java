package com.macedxs.mx.identity.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        Argon2PasswordEncoder encoder =
                Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        String password = "123456";

        System.out.println(encoder.encode(password));
    }
}
