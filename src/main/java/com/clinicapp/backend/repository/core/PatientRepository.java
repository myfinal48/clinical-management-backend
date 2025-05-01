package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Find patient by phone number (assuming it's unique)
    Optional<Patient> findByPhoneNumber(String phoneNumber);

    // Find patient by email (if unique constraint is enforced)
    Optional<Patient> findByEmail(String email);

    // Potential future methods:
    // List<Patient> findByLastNameContainingIgnoreCase(String lastName);
}