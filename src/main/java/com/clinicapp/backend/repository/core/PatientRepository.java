package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPhoneNumber(String phoneNumber);
    Optional<Patient> findByEmail(String email);
}