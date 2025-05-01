package com.clinicapp.backend.controller.auth;

import com.clinicapp.backend.dto.auth.AuthResponse;
import com.clinicapp.backend.dto.auth.LoginRequest;
import com.clinicapp.backend.dto.auth.RegisterRequest;
import com.clinicapp.backend.service.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth") // Base path for authentication endpoints
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request // Validate the request body
    ) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException e) {
            // Handle cases like username/email already exists
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // Catch other potential exceptions during registration
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed", e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request // Validate the request body
    ) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
             // Handles AuthenticationException and others
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed: Invalid credentials", e);
        }
    }
}