package com.clinicapp.backend.controller;

import com.clinicapp.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/secretary/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SECRETARY')") // Ensure only SECRETARY can access
public class SecretaryDashboardController {
    private final DashboardService dashboardService;
}
