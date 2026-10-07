package com.medipoint.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("LoginRequest should fail validation when email is invalid or password empty")
    void testLoginRequestValidation() {
        LoginRequest invalidRequest = new LoginRequest("not-an-email", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(invalidRequest);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));

        LoginRequest validRequest = new LoginRequest("owner@medipoint.com", "validpassword");
        Set<ConstraintViolation<LoginRequest>> validViolations = validator.validate(validRequest);
        assertTrue(validViolations.isEmpty());
    }

    @Test
    @DisplayName("RegisterRequest should validate fullName, email format, and password length")
    void testRegisterRequestValidation() {
        RegisterRequest invalidRequest = new RegisterRequest("", "bad-email", "+91 999", "123", null);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(invalidRequest);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));

        RegisterRequest validRequest = new RegisterRequest("Valid Name", "valid@medipoint.com", "+91 9999999999", "validpass123", null);
        Set<ConstraintViolation<RegisterRequest>> validViolations = validator.validate(validRequest);
        assertTrue(validViolations.isEmpty());
    }
}
