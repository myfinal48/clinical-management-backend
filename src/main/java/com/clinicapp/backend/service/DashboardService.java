package com.clinicapp.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {

    // --- Admin Stats ---
    long getTotalPatients();
    long getActiveStaffCount(); // Doctors + Secretaries

}