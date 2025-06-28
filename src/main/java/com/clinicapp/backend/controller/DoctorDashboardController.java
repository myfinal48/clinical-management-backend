package com.clinicapp.backend.controller;

import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // To get logged-in user
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctor/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')") // Ensure only DOCTOR can access
public class DoctorDashboardController {

    private final DashboardService dashboardService;

}