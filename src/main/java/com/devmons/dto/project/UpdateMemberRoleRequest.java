package com.devmons.dto.project;

import com.devmons.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a member's role in a project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRoleRequest {
    
    /**
     * New role for the member
     */
    @NotNull(message = "Role is required")
    private ProjectRole role;
}

