package com.devmons.service;

import com.devmons.dto.auth.*;
import com.devmons.entity.User;
import com.devmons.repository.UserRepository;
import com.devmons.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * 
 * Tests User Story #1 acceptance criteria:
 * - Registration with validation
 * - Email verification
 * - Login with JWT token generation
 * - Account lockout after failed attempts
 * - Password reset functionality
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecurePass123!")
                .fullName("Test User")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .emailVerified(true)
                .enabled(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Registration successful"));
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testRegister_DuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(registerRequest)
        );
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(registerRequest)
        );
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_Success() {
        // Arrange
        String token = "valid-token";
        testUser.setEmailVerified(false);
        testUser.setVerificationToken(token);
        testUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.verifyEmail(token);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("verified successfully"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testVerifyEmail_ExpiredToken() {
        // Arrange
        String token = "expired-token";
        testUser.setVerificationToken(token);
        testUser.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1));

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyEmail(token)
        );
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("SecurePass123!")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(testUser.getId(), response.getUserId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLogin_AccountLocked() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("WrongPassword")
                .build();

        testUser.setAccountLocked(true);
        testUser.setLockoutTime(LocalDateTime.now());

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        LockedException exception = assertThrows(
                LockedException.class,
                () -> authService.login(loginRequest)
        );
        assertTrue(exception.getMessage().contains("locked"));
    }

    @Test
    void testLogin_EmailNotVerified() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("SecurePass123!")
                .build();

        testUser.setEmailVerified(false);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );
        assertTrue(exception.getMessage().contains("not verified"));
    }

    @Test
    void testRequestPasswordReset_Success() {
        // Arrange
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("test@example.com")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.requestPasswordReset(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("reset link sent"));
        verify(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void testResetPassword_Success() {
        // Arrange
        String token = "reset-token";
        PasswordResetConfirm request = PasswordResetConfirm.builder()
                .token(token)
                .newPassword("NewSecurePass123!")
                .build();

        testUser.setPasswordResetToken(token);
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.resetPassword(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("reset successfully"));
        verify(userRepository).save(any(User.class));
    }
}

