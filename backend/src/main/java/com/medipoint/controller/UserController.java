package com.medipoint.controller;

import com.medipoint.dto.ApiResponse;
import com.medipoint.dto.UserResponse;
import com.medipoint.exception.CustomException;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/user", "/api/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw CustomException.unauthorized("Authentication required to access user profile.");
        }
        UserResponse profile = userService.getUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved successfully.", profile));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok("User list retrieved successfully.", users));
    }
}
