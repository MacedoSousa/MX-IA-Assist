package com.macedxs.mx.identity.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void cors_shouldAllowOnlyTheKnownMxWebLoopbackOrigins() {
        CorsConfiguration configuration = corsConfigurationSource
                .getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/auth/login"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://127.0.0.1:8082"))
                .isEqualTo("http://127.0.0.1:8082");
        assertThat(configuration.checkOrigin("http://localhost:8082"))
                .isEqualTo("http://localhost:8082");
        assertThat(configuration.checkOrigin("http://127.0.0.1:18080")).isNull();
        assertThat(configuration.checkOrigin("https://untrusted.example")).isNull();
    }
}
