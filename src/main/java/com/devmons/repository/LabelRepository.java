package com.devmons.repository;

import com.devmons.entity.Label;
import com.devmons.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Label entity.
 */
@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    
    /**
     * Find all labels for a project
     */
    List<Label> findByProject(Project project);
}

