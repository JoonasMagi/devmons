package com.devmons.repository;

import com.devmons.entity.Issue;
import com.devmons.entity.Project;
import com.devmons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Issue entity.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    
    /**
     * Find issue by key
     */
    Optional<Issue> findByKey(String key);
    
    /**
     * Find all issues for a project
     */
    List<Issue> findByProject(Project project);
    
    /**
     * Find all issues for a project ordered by number descending
     */
    List<Issue> findByProjectOrderByNumberDesc(Project project);
    
    /**
     * Find highest issue number for a project
     */
    @Query("SELECT MAX(i.number) FROM Issue i WHERE i.project = :project")
    Integer findMaxNumberByProject(@Param("project") Project project);
    
    /**
     * Find all issues assigned to a user
     */
    List<Issue> findByAssignee(User assignee);
    
    /**
     * Find all issues reported by a user
     */
    List<Issue> findByReporter(User reporter);
    
    /**
     * Find all issues for a project assigned to a user
     */
    List<Issue> findByProjectAndAssignee(Project project, User assignee);
    
    /**
     * Check if issue key exists
     */
    boolean existsByKey(String key);

    /**
     * Find backlog issues ordered by backlog position
     */
    @Query("SELECT i FROM Issue i WHERE i.project = :project ORDER BY COALESCE(i.backlogPosition, 999999), i.createdAt")
    List<Issue> findBacklogIssues(@Param("project") Project project);
}

