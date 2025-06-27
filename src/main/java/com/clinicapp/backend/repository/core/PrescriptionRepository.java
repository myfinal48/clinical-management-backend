package com.clinicapp.backend.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinicapp.backend.model.core.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
}