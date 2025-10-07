package com.devmons.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity representing system users.
 * 
 * Stores authentication credentials and profile information.
 * Links to projects through ProjectMember relationships.
 * 
 * Security requirements (Section 5.3):
 * - Passwords stored using bcrypt hashing
 * - Account lockout after 5 failed login attempts
 * - Email verification required for activation
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for login.
     * Must be unique across the system.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * User's email address.
     * Must be unique and valid format.
     * Used for notifications and password reset.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Bcrypt hashed password.
     * Minimum requirements: 12 characters, mixed case, numbers, special characters.
     * Never stored in plain text.
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * User's full name for display purposes.
     */
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(length = 100)
    private String fullName;

    /**
     * URL to user's avatar image.
     * Stored in object storage (S3/MinIO).
     */
    @Column(length = 500)
    private String avatarUrl;

    /**
     * Account creation timestamp.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last successful login timestamp.
     * Updated on each successful authentication.
     */
    private LocalDateTime lastLogin;

    /**
     * Email verification status.
     * Account must be verified before full access.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * Email verification token.
     * Generated during registration, expires after 24 hours.
     */
    @Column(length = 255)
    private String verificationToken;

    /**
     * Verification token expiration timestamp.
     */
    private LocalDateTime verificationTokenExpiry;

    /**
     * Password reset token.
     * Generated on password reset request, expires after 24 hours.
     */
    @Column(length = 255)
    private String passwordResetToken;

    /**
     * Password reset token expiration timestamp.
     */
    private LocalDateTime passwordResetTokenExpiry;

    /**
     * Account enabled status.
     * Disabled accounts cannot log in.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Account locked status.
     * Locked after 5 failed login attempts.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    /**
     * Failed login attempt counter.
     * Reset on successful login.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * Account lockout timestamp.
     * Account unlocks automatically after 15 minutes.
     */
    private LocalDateTime lockoutTime;

    /**
     * Set creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if account is locked and should be unlocked.
     * Accounts unlock after 15 minutes (900000ms).
     */
    public boolean isAccountNonLocked() {
        if (!accountLocked) {
            return true;
        }
        
        if (lockoutTime != null) {
            LocalDateTime unlockTime = lockoutTime.plusMinutes(15);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                // Auto-unlock account
                accountLocked = false;
                failedLoginAttempts = 0;
                lockoutTime = null;
                return true;
            }
        }
        
        return false;
    }

    /**
     * Increment failed login attempts.
     * Lock account if threshold (5 attempts) is reached.
     */
    public void incrementFailedAttempts() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            accountLocked = true;
            lockoutTime = LocalDateTime.now();
        }
    }

    /**
     * Reset failed login attempts on successful login.
     */
    public void resetFailedAttempts() {
        failedLoginAttempts = 0;
        accountLocked = false;
        lockoutTime = null;
        lastLogin = LocalDateTime.now();
    }
}

