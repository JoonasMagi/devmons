package com.devmons.repository;

import com.devmons.entity.Project;
import com.devmons.entity.ProjectMember;
import com.devmons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProjectMember entity.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    
    /**
     * Find all members of a project
     */
    List<ProjectMember> findByProject(Project project);
    
    /**
     * Find membership for a specific user in a project
     */
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    
    /**
     * Check if user is a member of a project
     */
    boolean existsByProjectAndUser(Project project, User user);
}

