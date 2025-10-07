package com.devmons.entity;

/**
 * Enum representing roles a user can have in a project.
 * 
 * OWNER: Full project access, can configure settings, manage members
 * MEMBER: Can create and edit issues, participate in sprints
 * VIEWER: Read-only access to project
 */
public enum ProjectRole {
    OWNER,
    MEMBER,
    VIEWER
}

