package com.devmons.repository;

import com.devmons.entity.Project;
import com.devmons.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for WorkflowState entity.
 */
@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {
    
    /**
     * Find all workflow states for a project, ordered by order field
     */
    List<WorkflowState> findByProjectOrderByOrderAsc(Project project);
}

