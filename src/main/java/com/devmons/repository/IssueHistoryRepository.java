package com.devmons.repository;

import com.devmons.entity.Issue;
import com.devmons.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IssueHistory entity.
 */
@Repository
public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {
    
    /**
     * Find all history entries for an issue, ordered by change time descending
     */
    List<IssueHistory> findByIssueOrderByChangedAtDesc(Issue issue);
}

