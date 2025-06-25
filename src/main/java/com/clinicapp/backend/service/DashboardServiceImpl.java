package com.clinicapp.backend.service;

// Core Models (Assuming package structure)
// Security Models
import com.clinicapp.backend.model.security.Role; // Import Role
// Repositories
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Most dashboard methods are read-only
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    // --- Admin Stats ---

    @Override
    public long getTotalPatients() {
        return patientRepository.count();
    }

    @Override
    public long getActiveStaffCount() {
        // Assuming all users in DB are active for now
        return userRepository.countByRoleIn(List.of(Role.DOCTOR, Role.SECRETARY)); // Requires Role model import
    }
    }