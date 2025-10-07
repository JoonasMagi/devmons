package com.devmons.repository;

import com.devmons.entity.IssueType;
import com.devmons.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IssueType entity.
 */
@Repository
public interface IssueTypeRepository extends JpaRepository<IssueType, Long> {
    
    /**
     * Find all issue types for a project
     */
    List<IssueType> findByProject(Project project);
}

