package com.medipoint.service;

import com.medipoint.dto.AuthResponse;
import com.medipoint.dto.LoginRequest;
import com.medipoint.dto.RegisterRequest;
import com.medipoint.dto.UserResponse;
import com.medipoint.exception.CustomException;
import com.medipoint.model.User;
import com.medipoint.model.UserRole;
import com.medipoint.model.UserStatus;
import com.medipoint.repository.UserRepository;
import com.medipoint.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials and issue JWT")
    void testSuccessfulAuthentication() {
        User user = new User("Store Owner", "owner@medipoint.com", "+91 9876543210", "$2a$10$hashedPassword", UserRole.OWNER, UserStatus.ACTIVE);
        user.setId(1L);

        when(userRepository.findByEmailIgnoreCase("owner@medipoint.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("owner123", "$2a$10$hashedPassword")).thenReturn(true);

        LoginRequest request = new LoginRequest("owner@medipoint.com", "owner123");
        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
        assertEquals("owner@medipoint.com", response.getEmail());
        assertEquals(UserRole.OWNER, response.getRole());
        assertEquals(1L, response.getId());
        verify(userRepository, times(1)).findByEmailIgnoreCase("owner@medipoint.com");
    }

    @Test
    @DisplayName("Should throw generic unauthorized exception on invalid password")
    void testAuthenticationFailsOnBadPassword() {
        User user = new User("Store Owner", "owner@medipoint.com", "+91 9876543210", "$2a$10$hashedPassword", UserRole.OWNER, UserStatus.ACTIVE);
        user.setId(1L);

        when(userRepository.findByEmailIgnoreCase("owner@medipoint.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "$2a$10$hashedPassword")).thenReturn(false);

        LoginRequest request = new LoginRequest("owner@medipoint.com", "wrong_password");

        CustomException exception = assertThrows(CustomException.class, () -> authService.authenticate(request));
        assertEquals("Invalid email or password.", exception.getMessage());
        assertEquals(401, exception.getStatus().value());
    }

    @Test
    @DisplayName("Should throw generic unauthorized exception when user is not found")
    void testAuthenticationFailsOnUnknownUser() {
        when(userRepository.findByEmailIgnoreCase("unknown@medipoint.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@medipoint.com", "any_password");

        CustomException exception = assertThrows(CustomException.class, () -> authService.authenticate(request));
        assertEquals("Invalid email or password.", exception.getMessage());
        assertEquals(401, exception.getStatus().value());
    }

    @Test
    @DisplayName("Should reject authentication for inactive user accounts")
    void testAuthenticationFailsOnInactiveUser() {
        User inactiveUser = new User("Former Worker", "worker@medipoint.com", "+91 9876543211", "$2a$10$hashedPassword", UserRole.WORKER, UserStatus.INACTIVE);
        inactiveUser.setId(2L);

        when(userRepository.findByEmailIgnoreCase("worker@medipoint.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("worker123", "$2a$10$hashedPassword")).thenReturn(true);

        LoginRequest request = new LoginRequest("worker@medipoint.com", "worker123");

        CustomException exception = assertThrows(CustomException.class, () -> authService.authenticate(request));
        assertTrue(exception.getMessage().contains("inactive"));
        assertEquals(403, exception.getStatus().value());
    }

    @Test
    @DisplayName("Should register new user with hashed password")
    void testSuccessfulRegistration() {
        when(userRepository.existsByEmailIgnoreCase("newworker@medipoint.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$newlyHashedPassword");

        User savedUser = new User("New Worker", "newworker@medipoint.com", "+91 7777777777", "$2a$10$newlyHashedPassword", UserRole.WORKER, UserStatus.ACTIVE);
        savedUser.setId(10L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterRequest request = new RegisterRequest("New Worker", "newworker@medipoint.com", "+91 7777777777", "secret123", UserRole.WORKER);
        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("newworker@medipoint.com", response.getEmail());
        assertEquals(UserRole.WORKER, response.getRole());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        verify(passwordEncoder, times(1)).encode("secret123");
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegistrationRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("owner@medipoint.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("Duplicate Owner", "owner@medipoint.com", "+91 9999999999", "pass123", UserRole.OWNER);

        CustomException exception = assertThrows(CustomException.class, () -> authService.register(request));
        assertTrue(exception.getMessage().contains("already exists"));
        assertEquals(400, exception.getStatus().value());
        verify(userRepository, never()).save(any(User.class));
    }
}
