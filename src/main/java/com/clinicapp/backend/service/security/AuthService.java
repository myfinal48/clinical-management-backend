package com.clinicapp.backend.service.security;

import com.clinicapp.backend.dto.auth.AuthResponse;
import com.clinicapp.backend.dto.auth.LoginRequest;
import com.clinicapp.backend.dto.auth.RegisterRequest;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.security.UserRepository;
import com.clinicapp.backend.service.core.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    /**
     * Registers a new user.
     *
     * @param request The registration request details.
     * @return AuthResponse containing the JWT token for the newly registered user.
     * @throws IllegalArgumentException if username or email already exists.
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encode password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();

        userRepository.save(user); // Save the new user

        // Log user registration
        auditService.logRegistration(user.getId(), user.getUsername(), user.getRole().name());

        // Generate JWT token for the new user
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request The login request details.
     * @return AuthResponse containing the JWT token upon successful authentication.
     * @throws AuthenticationException if authentication fails.
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            // This will use our UserDetailsServiceImpl and PasswordEncoder
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), // Use email from LoginRequest
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            // Log failed login attempt
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();
            auditService.logFailedLogin(request.getEmail(), ipAddress, userAgent, e.getMessage());
            throw e; // Re-throw the exception
        }

        // If authentication is successful, find the user by email
        var user = userRepository.findByEmail(request.getEmail()) // Find by email
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication")); // Should not happen

        // Log successful login
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();
        auditService.logLogin(user.getId(), user.getUsername(), user.getRole().name(), ipAddress, userAgent);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(user) // Add the user object to the response
                .build();
    }

    // Utility methods to get request information
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Log warning but don't fail the operation
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Log warning but don't fail the operation
        }
        return "unknown";
    }
}