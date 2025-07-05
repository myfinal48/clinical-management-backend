package com.clinicapp.backend.controller;

import com.clinicapp.backend.dto.auth.UserResponseDTO;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')")
    public ResponseEntity<List<UserResponseDTO>> getStaff(@RequestParam(required = false) Role role) {
        List<UserResponseDTO> staff = (role == null) ? userService.getAllUsers() : userService.getUsersByRole(role);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')")
    public ResponseEntity<UserResponseDTO> getStaffById(@PathVariable Long id) {
        UserResponseDTO staff = userService.getUserById(id);
        return ResponseEntity.ok(staff);
    }

}