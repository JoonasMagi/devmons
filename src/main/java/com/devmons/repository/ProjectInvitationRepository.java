package com.devmons.repository;

import com.devmons.entity.InvitationStatus;
import com.devmons.entity.Project;
import com.devmons.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProjectInvitation entity.
 */
@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    
    /**
     * Find invitation by token
     */
    Optional<ProjectInvitation> findByToken(String token);
    
    /**
     * Find all invitations for a project
     */
    List<ProjectInvitation> findByProject(Project project);
    
    /**
     * Find all pending invitations for a project
     */
    List<ProjectInvitation> findByProjectAndStatus(Project project, InvitationStatus status);
    
    /**
     * Find pending invitation by project and email
     */
    Optional<ProjectInvitation> findByProjectAndEmailAndStatus(Project project, String email, InvitationStatus status);
    
    /**
     * Check if there's a pending invitation for email in project
     */
    boolean existsByProjectAndEmailAndStatus(Project project, String email, InvitationStatus status);
}

