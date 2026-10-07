package com.medipoint.dto;

import com.medipoint.model.UserRole;
import com.medipoint.model.UserStatus;

import java.util.List;

public class AuthResponse {

    private String token;
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private List<String> permissions;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long id, String email, String fullName, UserRole role, UserStatus status, List<String> permissions) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
