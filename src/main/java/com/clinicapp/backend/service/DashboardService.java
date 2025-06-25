package com.clinicapp.backend.service;


public interface DashboardService {

    // --- Admin Stats ---
    long getTotalPatients();
    long getActiveStaffCount(); // Doctors + Secretaries

}