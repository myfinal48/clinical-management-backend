package com.clinicapp.backend.dto.auth;

import com.clinicapp.backend.model.security.User; // Import User model
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token; // The JWT token
    private User user; // Add the user details field
    // Optionally, you could include the refresh token here as well
    // private String refreshToken;
}