package com.devmons.dto.project;

import com.devmons.entity.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for project member response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}

