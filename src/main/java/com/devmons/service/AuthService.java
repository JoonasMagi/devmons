package com.devmons.service;

import com.devmons.dto.auth.*;
import com.devmons.entity.User;
import com.devmons.repository.UserRepository;
import com.devmons.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for authentication operations.
 * 
 * Implements User Story #1: User Registration and Authentication
 * - User registration with email verification
 * - Secure login with JWT tokens
 * - Password reset functionality
 * - Account lockout after failed attempts
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Register a new user.
     * 
     * Acceptance Criteria:
     * - Validates email format
     * - Checks for duplicate username/email
     * - Password meets minimum requirements (validated by DTO)
     * - Generates email verification token
     * - Sends verification email
     * 
     * @param request registration request
     * @return success message
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public String register(RegisterRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();

        // Create user (auto-verify email in development to avoid SMTP setup)
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .emailVerified(true) // Auto-verify for development
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .enabled(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);

        // Try to send verification email (fail silently in development if SMTP not configured)
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        } catch (Exception e) {
            // Log but don't fail registration if email sending fails (development mode)
            System.out.println("Email sending failed (development mode): " + e.getMessage());
        }

        return "Registration successful. You can now log in.";
    }

    /**
     * Verify user email with token.
     * 
     * Acceptance Criteria:
     * - Validates verification token
     * - Checks token expiration (24 hours)
     * - Activates account after verification
     * 
     * @param token verification token
     * @return success message
     * @throws IllegalArgumentException if token is invalid or expired
     */
    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        // Check if token is expired
        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        // Verify email
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return "Email verified successfully. You can now log in.";
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * Acceptance Criteria:
     * - Accepts username or email for login
     * - Validates credentials
     * - Generates JWT token with 8-hour expiration
     * - Locks account after 5 failed attempts
     * - Redirects to dashboard after successful login (handled by frontend)
     * 
     * @param request login request
     * @return authentication response with JWT token
     * @throws BadCredentialsException if credentials are invalid
     * @throws LockedException if account is locked
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by username or email
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new LockedException("Account is locked due to multiple failed login attempts. Please try again later.");
        }

        // Email verification check disabled for development
        // In production, uncomment this:
        // if (!user.getEmailVerified()) {
        //     throw new BadCredentialsException("Email not verified. Please check your email for verification link.");
        // }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts on successful login
            user.resetFailedAttempts();
            userRepository.save(user);

            // Generate JWT token
            String jwt = jwtTokenProvider.generateToken(authentication);

            return AuthResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

        } catch (AuthenticationException e) {
            // Increment failed attempts
            user.incrementFailedAttempts();
            userRepository.save(user);

            if (user.getAccountLocked()) {
                throw new LockedException("Account locked due to multiple failed login attempts. Please try again in 15 minutes.");
            }

            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    /**
     * Request password reset.
     * 
     * Acceptance Criteria:
     * - Validates email exists
     * - Generates password reset token
     * - Sends reset link via email
     * - Token expires after 24 hours
     * 
     * @param request password reset request
     * @return success message
     */
    @Transactional
    public String requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return "Password reset link sent to your email";
    }

    /**
     * Reset password with token.
     * 
     * Acceptance Criteria:
     * - Validates reset token
     * - Checks token expiration
     * - Updates password securely (bcrypt)
     * - New password meets requirements (validated by DTO)
     * 
     * @param request password reset confirmation
     * @return success message
     * @throws IllegalArgumentException if token is invalid or expired
     */
    @Transactional
    public String resetPassword(PasswordResetConfirm request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        // Check if token is expired
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return "Password reset successfully";
    }
}

