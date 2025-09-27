package com.clinicapp.backend.service;

import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Override
    public long getTotalPatients() {
        return patientRepository.count();
    }

    @Override
    public long getActiveStaffCount() {
        return userRepository.countByRoleIn(List.of(Role.DOCTOR, Role.SECRETARY));
    }

    @Override
    public Object getStats() {
        return new java.util.HashMap<String, Object>();
    }
}