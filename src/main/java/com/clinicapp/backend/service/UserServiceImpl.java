package com.clinicapp.backend.service;

import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.clinicapp.backend.dto.auth.RegisterRequest;
import com.clinicapp.backend.dto.auth.UpdateUserRequestDTO;
import com.clinicapp.backend.dto.auth.UserResponseDTO;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import com.clinicapp.backend.exceptions.BadRequestException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(RegisterRequest request) {
        Objects.requireNonNull(request, "User cannot be null");
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Password cannot be empty for new user");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }
        if (StringUtils.hasText(request.getUsername()) && userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();
        User saved = userRepository.save(user);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO userUpdates) {
        Objects.requireNonNull(userId, "User ID cannot be null for update");
        Objects.requireNonNull(userUpdates, "User updates cannot be null");

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        try {
            if (StringUtils.hasText(userUpdates.getUsername()) && !existingUser.getUsername().equals(userUpdates.getUsername())) {
                userRepository.findByUsername(userUpdates.getUsername()).ifPresent(u -> {
                    throw new BadRequestException("Username already exists: " + userUpdates.getUsername());
                });
                existingUser.setUsername(userUpdates.getUsername());
            }

            if (StringUtils.hasText(userUpdates.getEmail()) && !existingUser.getEmail().equals(userUpdates.getEmail())) {
                userRepository.findByEmail(userUpdates.getEmail()).ifPresent(u -> {
                    throw new BadRequestException("Email already exists: " + userUpdates.getEmail());
                });
                existingUser.setEmail(userUpdates.getEmail());
            }

            if (StringUtils.hasText(userUpdates.getFirstName())) {
                existingUser.setFirstName(userUpdates.getFirstName());
            }

            if (StringUtils.hasText(userUpdates.getLastName())) {
                existingUser.setLastName(userUpdates.getLastName());
            }

            if (userUpdates.getRole() != null) {
                existingUser.setRole(userUpdates.getRole());
            }

            User updated = userRepository.save(existingUser);
            return mapToResponseDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Failed to update user due to data integrity violation. The username or email may already be in use.");
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null for filtering");
        return userRepository.findByRole(role).stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}