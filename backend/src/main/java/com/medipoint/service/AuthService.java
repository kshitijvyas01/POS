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
import com.medipoint.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw CustomException.badRequest("Email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw CustomException.badRequest("Password is required.");
        }

        String email = request.getEmail().trim().toLowerCase();

        // 1. Look up user by email in MySQL
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: No user found with email [{}]", email);
                    return CustomException.unauthorized("Invalid email or password.");
                });

        // 2. Verify hashed password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Authentication failed: Incorrect password for user [{}]", email);
            throw CustomException.unauthorized("Invalid email or password.");
        }

        // 3. Verify user account status
        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.warn("Authentication failed: Inactive account attempted login [{}]", email);
            throw CustomException.forbidden("Your account is currently inactive. Please contact store owner.");
        }

        // 4. Generate JWT
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String token = jwtService.generateToken(userPrincipal);

        logger.info("User [{}] authenticated successfully with role [{}].", email, user.getRole());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                userPrincipal.getPermissions()
        );
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw CustomException.badRequest("An account with email '" + email + "' already exists.");
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.WORKER;

        User user = new User(
                request.getFullName().trim(),
                email,
                request.getPhone() != null ? request.getPhone().trim() : null,
                passwordEncoder.encode(request.getPassword()),
                role,
                UserStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);
        logger.info("New user registered successfully: [{}] with role [{}]", savedUser.getEmail(), savedUser.getRole());

        return UserResponse.fromUser(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw CustomException.unauthorized("User session is unauthenticated.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> CustomException.notFound("User profile not found."));

        return UserResponse.fromUser(user);
    }
}
