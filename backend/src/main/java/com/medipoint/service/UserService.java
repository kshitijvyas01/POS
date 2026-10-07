package com.medipoint.service;

import com.medipoint.dto.UserResponse;
import com.medipoint.exception.CustomException;
import com.medipoint.model.User;
import com.medipoint.model.UserRole;
import com.medipoint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("User not found with id: " + userId));
        return UserResponse.fromUser(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countOwners() {
        return userRepository.countByRole(UserRole.OWNER);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
