package com.devmons.repository;

import com.devmons.entity.Comment;
import com.devmons.entity.Mention;
import com.devmons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Mention entity.
 * 
 * Provides data access for user mentions in comments.
 * Used for notification delivery and mention tracking.
 * 
 * Related to User Story #7: Add Comments and Collaborate on Issues
 */
@Repository
public interface MentionRepository extends JpaRepository<Mention, Long> {
    
    /**
     * Find all mentions for a specific user.
     * 
     * Used to retrieve all mentions for notification display.
     * Spring Data JPA generates: SELECT * FROM mentions WHERE mentioned_user_id = ?
     * 
     * @param mentionedUser User who was mentioned
     * @return List of mentions for the user
     */
    List<Mention> findByMentionedUser(User mentionedUser);
    
    /**
     * Find all mentions in a specific comment.
     * 
     * Used to retrieve mentions when displaying comment details.
     * Spring Data JPA generates: SELECT * FROM mentions WHERE comment_id = ?
     * 
     * @param comment Comment to find mentions in
     * @return List of mentions in the comment
     */
    List<Mention> findByComment(Comment comment);
    
    /**
     * Delete all mentions in a comment.
     * 
     * Used when a comment is deleted or edited (mentions are re-parsed).
     * Spring Data JPA generates: DELETE FROM mentions WHERE comment_id = ?
     * 
     * @param comment Comment whose mentions should be deleted
     */
    void deleteByComment(Comment comment);
}

