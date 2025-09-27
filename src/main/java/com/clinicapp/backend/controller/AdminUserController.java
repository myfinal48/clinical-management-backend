package com.clinicapp.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.clinicapp.backend.dto.auth.RegisterRequest;
import com.clinicapp.backend.dto.auth.UpdateUserRequestDTO;
import com.clinicapp.backend.dto.auth.UserResponseDTO;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for admin-level user management operations.
 * Provides endpoints for creating, retrieving, updating, and deleting users.
 * Access is restricted to users with the ADMIN role.
 */
@Tag(name = "admin-user-controller", description = "Endpoints for managing users (Admin access only).")
@RestController
@RequestMapping("${api.prefix}/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    
    /**
     * Retrieves a list of all users, optionally filtered by role.
     * Accessible only by ADMIN role.
     *
     * @param role The optional role to filter users by.
     * @return A list of users.
     */
    @Operation(summary = "Get all users, optionally by role. Role: ADMIN.")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@RequestParam(required = false) Role role) {
        List<UserResponseDTO> users = (role == null) ? userService.getAllUsers() : userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a specific user by their ID.
     * Accessible only by ADMIN role.
     *
     * @param id The ID of the user to retrieve.
     * @return The user with the specified ID.
     */
    @Operation(summary = "Get a user by ID. Role: ADMIN.")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new user.
     * Accessible only by ADMIN role.
     *
     * @param user The user to create.
     * @return The created user.
     */
    @Operation(summary = "Create a new user. Role: ADMIN.")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody RegisterRequest user) {
        UserResponseDTO createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Updates an existing user by their ID.
     * @param id The ID of the user to update.
     * @param user The updated user data.
     * @return The updated user.
     */
    @Operation(summary = "Update a user by ID. Role: ADMIN.")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequestDTO user) {
        UserResponseDTO updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user by their ID.
     * Accessible only by ADMIN role.
     *
     */
    @Operation(summary = "Delete a user by ID. Role: ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}