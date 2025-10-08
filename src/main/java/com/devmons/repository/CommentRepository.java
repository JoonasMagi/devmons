package com.devmons.repository;

import com.devmons.entity.Comment;
import com.devmons.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity.
 *
 * Uses Spring Data JPA's Repository pattern for data access.
 * Method names follow Spring Data JPA naming conventions for automatic query generation.
 *
 * Related to User Story #7: Add Comments and Collaborate on Issues
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all comments for an issue, ordered by creation time (oldest first).
     *
     * This ensures comments appear in chronological order in the activity timeline.
     * Spring Data JPA automatically generates the query from the method name:
     * SELECT * FROM comments WHERE issue_id = ? ORDER BY created_at ASC
     *
     * @param issue Issue to find comments for
     * @return List of comments ordered by creation time (oldest first)
     */
    List<Comment> findByIssueOrderByCreatedAtAsc(Issue issue);

    /**
     * Count total number of comments for an issue.
     *
     * Useful for displaying comment count badges and statistics.
     * Spring Data JPA automatically generates: SELECT COUNT(*) FROM comments WHERE issue_id = ?
     *
     * @param issue Issue to count comments for
     * @return Total number of comments
     */
    long countByIssue(Issue issue);
}

