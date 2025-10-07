package com.devmons.controller;

import com.devmons.dto.auth.*;
import com.devmons.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication endpoints.
 * 
 * Implements User Story #1: User Registration and Authentication
 * 
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - GET /api/auth/verify - Verify email
 * - POST /api/auth/login - Login user
 * - POST /api/auth/password-reset/request - Request password reset
 * - POST /api/auth/password-reset/confirm - Confirm password reset
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     * 
     * Acceptance Criteria:
     * - User can enter username, email, and password
     * - Password must meet minimum requirements
     * - System validates email format
     * - System checks for duplicate username/email
     * - User receives email verification link
     * - Appropriate error messages shown for validation failures
     * 
     * @param request registration request
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verify user email.
     * 
     * Acceptance Criteria:
     * - Account is created after email verification
     * - Verification link expires after 24 hours
     * 
     * @param token verification token
     * @return success message
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        String message = authService.verifyEmail(token);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Login user.
     * 
     * Acceptance Criteria:
     * - User can log in with username/email and password
     * - System validates credentials
     * - JWT token is generated with 8-hour expiration
     * - Account locks after 5 failed login attempts
     * - Clear error messages for invalid credentials
     * 
     * @param request login request
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset.
     * 
     * Acceptance Criteria:
     * - User can request password reset via email
     * - System sends password reset link
     * 
     * @param request password reset request
     * @return success message
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        String message = authService.requestPasswordReset(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm password reset.
     * 
     * Acceptance Criteria:
     * - User can set new password via reset link
     * - Reset link expires after 24 hours
     * - New password meets requirements
     * 
     * @param request password reset confirmation
     * @return success message
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirm request) {
        String message = authService.resetPassword(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        
        return ResponseEntity.ok(response);
    }
}

