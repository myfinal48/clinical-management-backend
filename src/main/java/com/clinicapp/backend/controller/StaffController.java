package com.clinicapp.backend.controller;

import com.clinicapp.backend.dto.auth.UserResponseDTO;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * Controller for managing staff-related operations.
 * Provides endpoints for retrieving staff information.
 * Access is restricted to authenticated users with roles like ADMIN, SECRETARY, or DOCTOR.
 */
@Tag(name = "staff-controller", description = "Endpoints for retrieving staff information.")
@RestController
@RequestMapping("${api.prefix}/staff")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;
    /**
     * Retrieves a list of all staff members, optionally filtered by role.
     * If no role is specified, all users are returned.
     * Accessible by ADMIN, SECRETARY, and DOCTOR roles.
     *
     * @param role The optional role to filter staff by (e.g., DOCTOR, SECRETARY).
     * @return A list of staff members matching the criteria.
     */
    @Operation(summary = "Get staff list, optionally by role. Roles: ADMIN, SECRETARY, DOCTOR.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')")
    public ResponseEntity<List<UserResponseDTO>> getStaff(@RequestParam(required = false) Role role) {
        List<UserResponseDTO> staff = (role == null) ? userService.getAllUsers() : userService.getUsersByRole(role);
        return ResponseEntity.ok(staff);
    }

    /**
     * Retrieves a specific staff member by their ID.
     * Accessible by ADMIN, SECRETARY, and DOCTOR roles.
     *
     * @param id The ID of the staff member to retrieve.
     * @return The staff member matching the specified ID.
     */
    @Operation(summary = "Get staff member by ID. Roles: ADMIN, SECRETARY, DOCTOR.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')")
    public ResponseEntity<UserResponseDTO> getStaffById(@PathVariable Long id) {
        UserResponseDTO staff = userService.getUserById(id);
        return ResponseEntity.ok(staff);
    }
}