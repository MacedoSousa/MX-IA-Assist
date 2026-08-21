package com.macedxs.mx.identity.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsConfiguredLocalMxDomain() {
        var violations = validator.validate(new LoginRequest("pedro@mx.local", "secret", "test"));

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsAddressWithoutDomainSeparator() {
        var violations = validator.validate(new LoginRequest("pedro@mx", "secret", "test"));

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email");
    }
}
