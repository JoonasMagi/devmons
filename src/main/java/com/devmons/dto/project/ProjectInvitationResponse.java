package com.devmons.dto.project;

import com.devmons.entity.InvitationStatus;
import com.devmons.entity.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for project invitation response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInvitationResponse {
    
    private Long id;
    private Long projectId;
    private String projectName;
    private String email;
    private ProjectRole role;
    private InvitationStatus status;
    private String invitedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean expired;
}

