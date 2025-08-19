package com.clinicapp.backend.controller.auth;

import com.clinicapp.backend.dto.auth.AuthResponse;
import com.clinicapp.backend.dto.auth.LoginRequest;
import com.clinicapp.backend.dto.auth.RegisterRequest;
import com.clinicapp.backend.service.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user authentication processes.
 * Provides public endpoints for user registration and login.
 */
@Tag(name = "auth-controller", description = "Endpoints for user registration and login.")
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     *
     * @param request The registration request containing user details.
     * @return An authentication response with a JWT token.
     */
    @Operation(summary = "Register a new user.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authenticates an existing user and provides a JWT token.
     *
     * @param request The login request containing user credentials.
     * @return An authentication response with a JWT token.
     */
    @Operation(summary = "Log in an existing user.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}