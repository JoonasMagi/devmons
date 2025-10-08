package com.devmons.repository;

import com.devmons.entity.Notification;
import com.devmons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notification entity.
 * 
 * Provides methods to:
 * - Find notifications for a user
 * - Find unread notifications
 * - Count unread notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first).
     * 
     * @param user User to find notifications for
     * @return List of notifications
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find unread notifications for a user, ordered by creation date (newest first).
     * 
     * @param user User to find notifications for
     * @param isRead Read status (false for unread)
     * @return List of unread notifications
     */
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    
    /**
     * Count unread notifications for a user.
     * 
     * @param user User to count notifications for
     * @param isRead Read status (false for unread)
     * @return Number of unread notifications
     */
    Long countByUserAndIsRead(User user, Boolean isRead);
}

