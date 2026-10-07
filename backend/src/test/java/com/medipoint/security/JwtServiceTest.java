package com.medipoint.security;

import com.medipoint.model.User;
import com.medipoint.model.UserRole;
import com.medipoint.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 256-bit test secret
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate a valid JWT token with claims and extract username")
    void testGenerateAndValidateToken() {
        User user = new User("Test Owner", "owner@medipoint.com", "+91 9999999999", "hashedpass", UserRole.OWNER, UserStatus.ACTIVE);
        user.setId(101L);

        UserPrincipal principal = UserPrincipal.create(user);
        String token = jwtService.generateToken(principal);

        assertNotNull(token);
        assertFalse(token.isBlank());

        // Extract and verify claims
        String extractedEmail = jwtService.extractUsername(token);
        assertEquals("owner@medipoint.com", extractedEmail);

        Long extractedId = jwtService.extractUserId(token);
        assertEquals(101L, extractedId);

        String extractedRole = jwtService.extractRole(token);
        assertEquals("OWNER", extractedRole);

        // Validate token against user principal
        assertTrue(jwtService.isTokenValid(token, principal));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Should fail validation on tampered token")
    void testTamperedTokenFailsValidation() {
        User user = new User("Test Worker", "worker@medipoint.com", "+91 8888888888", "hashedpass", UserRole.WORKER, UserStatus.ACTIVE);
        user.setId(102L);

        UserPrincipal principal = UserPrincipal.create(user);
        String token = jwtService.generateToken(principal);

        // Alter token signature
        String tamperedToken = token + "xyz";

        assertFalse(jwtService.validateToken(tamperedToken));
        assertFalse(jwtService.isTokenValid(tamperedToken, principal));
    }
}
