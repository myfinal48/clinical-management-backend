package com.clinicapp.backend.controller;

import com.clinicapp.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN can access dashboard data
public class AdminDashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public Object getStats() {
        return dashboardService.getStats(); // Remplacez par la méthode réelle si besoin
    }
}