package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerAndAuthenticate_shouldWorkWithEncodedPassword() {
        UserEntity user = authService.register("Pedro", "pedro@example.com", "Secret123!");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("pedro@example.com");
        assertThat(user.getPasswordHash()).isNotBlank();
        assertThat(user.getPasswordHash()).isNotEqualTo("Secret123!");

        UserEntity authenticated = authService.authenticate("pedro@example.com", "Secret123!");
        assertThat(authenticated.getId()).isEqualTo(user.getId());
    }

    @Test
    void authenticate_shouldFailWithWrongPassword() {
        authService.register("Kevelin", "kevelin@example.com", "Another123!");

        assertThatThrownBy(() -> authService.authenticate("kevelin@example.com", "wrong-pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void authenticate_shouldFailWhenUserIsInactive() {
        UserEntity user = authService.register("Inativo", "inactive@example.com", "Strong123!");
        user.setActive(false);
        authService.save(user);

        assertThatThrownBy(() -> authService.authenticate("inactive@example.com", "Strong123!"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("inactive");
    }
}
