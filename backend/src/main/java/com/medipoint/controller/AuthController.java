package com.medipoint.controller;

import com.medipoint.dto.*;
import com.medipoint.exception.CustomException;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful. Welcome back to MediPoint!", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw CustomException.unauthorized("User session is unauthenticated or expired.");
        }
        UserResponse response = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.ok("Current user profile retrieved successfully.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully.", "Session terminated."));
    }
}
