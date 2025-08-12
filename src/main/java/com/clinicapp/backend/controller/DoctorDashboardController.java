package com.clinicapp.backend.controller;

import com.clinicapp.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/doctor/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDashboardController {

    private final DashboardService dashboardService;

}