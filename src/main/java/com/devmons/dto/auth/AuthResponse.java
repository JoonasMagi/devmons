package com.devmons.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response.
 * 
 * Contains JWT token and user information after successful login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
}

