package com.clinicapp.backend.controller;

import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/secretary/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SECRETARY')") // Ensure only SECRETARY can access
public class SecretaryDashboardController {
    private final DashboardService dashboardService;
}