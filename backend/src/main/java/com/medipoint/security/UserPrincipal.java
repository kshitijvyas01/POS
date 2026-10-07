package com.medipoint.security;

import com.medipoint.model.User;
import com.medipoint.model.UserRole;
import com.medipoint.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;
    private final List<String> permissions;

    public UserPrincipal(Long id, String fullName, String email, String password, UserRole role, UserStatus status, Collection<? extends GrantedAuthority> authorities, List<String> permissions) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.authorities = authorities;
        this.permissions = permissions;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        List<String> permissions = new ArrayList<>();
        if (user.getRole() == UserRole.OWNER) {
            permissions.addAll(List.of(
                "dashboard:read", "pos:access", "medicines:manage", "inventory:manage",
                "purchases:manage", "vendors:manage", "customers:manage", "sales:manage",
                "payments:manage", "invoices:manage", "analytics:read", "employees:manage",
                "attendance:manage", "leave:manage", "salary:manage", "alerts:manage",
                "settings:manage", "financials:read"
            ));
        } else {
            permissions.addAll(List.of(
                "dashboard:read", "pos:access", "medicines:read", "inventory:read",
                "customers:read_billing", "sales:own", "invoices:generate",
                "payments:collect", "attendance:own", "leave:own", "salary:own"
            ));
        }

        for (String perm : permissions) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }

        return new UserPrincipal(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getStatus(),
                authorities,
                permissions
        );
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
