package com.devmons.repository;

import com.devmons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity data access.
 * 
 * Provides CRUD operations and custom queries for user management.
 * Uses Spring Data JPA for database access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username.
     * Used for login authentication.
     * 
     * @param username the username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email address.
     * Used for email verification and password reset.
     * 
     * @param email the email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by email verification token.
     * Used during email verification process.
     * 
     * @param token the verification token
     * @return Optional containing user if found
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Find user by password reset token.
     * Used during password reset process.
     * 
     * @param token the password reset token
     * @return Optional containing user if found
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Check if username already exists.
     * Used for registration validation.
     * 
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email already exists.
     * Used for registration validation.
     * 
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);
}

