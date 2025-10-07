package com.devmons.dto.project;

import com.devmons.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for inviting a member to a project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {
    
    /**
     * Email address of user to invite
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    /**
     * Role to assign to the invited user
     */
    @NotNull(message = "Role is required")
    private ProjectRole role;
}

