package com.devmons.entity;

/**
 * Enum representing the status of a project invitation.
 * 
 * PENDING: Invitation sent, awaiting response
 * ACCEPTED: User accepted the invitation
 * DECLINED: User declined the invitation
 * CANCELLED: Owner cancelled the invitation
 * EXPIRED: Invitation expired (7 days)
 */
public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    EXPIRED
}

