package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.HospitalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalInfoRepository extends JpaRepository<HospitalInfo, Long> {
} 