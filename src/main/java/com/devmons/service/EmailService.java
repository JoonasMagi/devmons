package com.devmons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails.
 * 
 * Handles email verification and password reset emails.
 * Uses Spring Mail for SMTP integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send email verification link.
     * 
     * @param email recipient email
     * @param token verification token
     */
    public void sendVerificationEmail(String email, String token) {
        String subject = "DevMons - Verify Your Email";
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String message = "Thank you for registering with DevMons!\n\n" +
                "Please click the link below to verify your email address:\n" +
                verificationUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.";

        sendEmail(email, subject, message);
    }

    /**
     * Send password reset link.
     *
     * @param email recipient email
     * @param token reset token
     */
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "DevMons - Password Reset Request";
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        String message = "You have requested to reset your password.\n\n" +
                "Please click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request a password reset, please ignore this email.";

        sendEmail(email, subject, message);
    }

    /**
     * Send project invitation email.
     *
     * @param email recipient email
     * @param projectName name of the project
     * @param inviterName name of the person who sent the invitation
     * @param token invitation token
     */
    public void sendProjectInvitationEmail(String email, String projectName, String inviterName, String token) {
        String subject = "DevMons - Project Invitation: " + projectName;
        String invitationUrl = "http://localhost:3000/invitations/accept?token=" + token;
        String message = inviterName + " has invited you to join the project \"" + projectName + "\" on DevMons.\n\n" +
                "Please click the link below to accept the invitation:\n" +
                invitationUrl + "\n\n" +
                "This invitation will expire in 7 days.\n\n" +
                "If you do not wish to join this project, you can safely ignore this email.";

        sendEmail(email, subject, message);
    }

    /**
     * Send email using JavaMailSender.
     * 
     * @param to recipient email
     * @param subject email subject
     * @param text email body
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@devmons.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            // In production, you might want to queue failed emails for retry
        }
    }
}

