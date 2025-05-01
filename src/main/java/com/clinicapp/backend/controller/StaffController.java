package com.clinicapp.backend.controller;

import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.UserService; // Re-use existing UserService
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff") // New base path for staff-related info
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;

    // Endpoint specifically for getting doctors, accessible by multiple roles
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')") // Allow relevant roles
    public ResponseEntity<List<User>> getDoctors() {
        List<User> doctors = userService.getUsersByRole(Role.DOCTOR);
        // Consider returning a simpler DTO instead of the full User object if needed
        return ResponseEntity.ok(doctors);
    }

    // Could add endpoints for other roles or staff lists here if needed
}