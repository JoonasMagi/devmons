package com.devmons.repository;

import com.devmons.entity.Project;
import com.devmons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Find project by key
     */
    Optional<Project> findByKey(String key);
    
    /**
     * Check if project key exists
     */
    boolean existsByKey(String key);
    
    /**
     * Find all projects owned by a user
     */
    List<Project> findByOwner(User owner);
    
    /**
     * Find all projects owned by a user (not archived)
     */
    List<Project> findByOwnerAndArchivedFalse(User owner);
    
    /**
     * Find all projects owned by a user (archived only)
     */
    List<Project> findByOwnerAndArchivedTrue(User owner);
    
    /**
     * Find all projects where user is a member (including owner)
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members m " +
           "WHERE p.owner = :user OR m.user = :user")
    List<Project> findAllByUser(@Param("user") User user);
    
    /**
     * Find all non-archived projects where user is a member
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members m " +
           "WHERE (p.owner = :user OR m.user = :user) AND p.archived = false")
    List<Project> findAllActiveByUser(@Param("user") User user);
}

