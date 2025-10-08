package com.devmons.entity;

/**
 * Types of notifications that can be sent to users.
 */
public enum NotificationType {
    /**
     * User was mentioned in a comment (@username).
     */
    MENTION,
    
    /**
     * User was assigned to an issue.
     */
    ASSIGNMENT,
    
    /**
     * Issue status changed for an issue the user is assigned to.
     */
    STATUS_CHANGE,
    
    /**
     * Comment was added to an issue the user is watching.
     */
    COMMENT_ADDED,
    
    /**
     * Issue was updated (title, description, priority, etc.).
     */
    ISSUE_UPDATED
}

