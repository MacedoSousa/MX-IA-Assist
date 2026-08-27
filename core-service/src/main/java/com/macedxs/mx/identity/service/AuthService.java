package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserEntity register(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must have at least 8 characters");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setActive(true);

        return userRepository.save(user);
    }

    @Transactional
    public UserEntity authenticate(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException();
        }

        UserEntity user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
}
