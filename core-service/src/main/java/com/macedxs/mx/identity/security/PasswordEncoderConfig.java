package com.macedxs.mx.identity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public Argon2PasswordEncoder passwordEncoder() {
        System.out.println("### ARGON2 ENCODER ATIVO ###");
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
