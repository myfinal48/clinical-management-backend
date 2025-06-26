package com.clinicapp.backend.service;

import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.dto.auth.RegisterRequest;
import com.clinicapp.backend.dto.auth.UpdateUserRequestDTO;
import com.clinicapp.backend.dto.auth.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(RegisterRequest user);

    UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO user);

    void deleteUser(Long userId);

    UserResponseDTO getUserById(Long userId);

    List<UserResponseDTO> getAllUsers();

    List<UserResponseDTO> getUsersByRole(Role role);

    // Potentially add methods for password reset, enabling/disabling users etc.
}