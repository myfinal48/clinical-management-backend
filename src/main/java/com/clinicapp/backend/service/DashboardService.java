package com.clinicapp.backend.service;


public interface DashboardService {

    long getTotalPatients();
    long getActiveStaffCount();
    Object getStats();

}