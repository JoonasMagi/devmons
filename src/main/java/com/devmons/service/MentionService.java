package com.devmons.service;

import com.devmons.entity.Comment;
import com.devmons.entity.Mention;
import com.devmons.entity.User;
import com.devmons.repository.MentionRepository;
import com.devmons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing user mentions in comments.
 * 
 * Handles:
 * - Parsing @username mentions from comment text
 * - Creating mention records
 * - Sending notifications to mentioned users
 * 
 * Related to User Story #7: Add Comments and Collaborate on Issues
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MentionService {

    private final MentionRepository mentionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    /**
     * Regular expression to match @username mentions.
     * 
     * Pattern: @[a-zA-Z0-9_]{3,50}
     * - @ symbol
     * - Followed by 3-50 alphanumeric characters or underscores
     * - Matches valid usernames as defined in User entity
     */
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]{3,50})");
    
    /**
     * Parse mentions from comment content and create mention records.
     * 
     * Process:
     * 1. Extract all @username patterns from comment text
     * 2. Look up each username in database
     * 3. Create Mention record for each valid user
     * 4. Skip duplicates (same user mentioned multiple times)
     * 5. Skip self-mentions (author mentioning themselves)
     * 
     * @param comment Comment to parse mentions from
     * @return List of created mentions
     */
    @Transactional
    public List<Mention> processMentions(Comment comment) {
        List<Mention> mentions = new ArrayList<>();
        Set<String> processedUsernames = new HashSet<>();
        
        // Extract all @username mentions from comment content
        Matcher matcher = MENTION_PATTERN.matcher(comment.getContent());
        
        while (matcher.find()) {
            String username = matcher.group(1); // Extract username without @
            
            // Skip if already processed (avoid duplicate mentions)
            if (processedUsernames.contains(username)) {
                continue;
            }
            
            // Skip self-mentions (author mentioning themselves)
            if (username.equals(comment.getAuthor().getUsername())) {
                log.debug("Skipping self-mention: @{}", username);
                continue;
            }
            
            // Look up user by username
            userRepository.findByUsername(username).ifPresent(mentionedUser -> {
                // Create mention record
                Mention mention = Mention.builder()
                    .comment(comment)
                    .mentionedUser(mentionedUser)
                    .mentionedBy(comment.getAuthor())
                    .build();
                
                mention = mentionRepository.save(mention);
                mentions.add(mention);
                processedUsernames.add(username);

                // Create notification for mentioned user
                notificationService.createMentionNotification(mention, comment);

                log.info("Created mention: @{} mentioned by {} in comment {}",
                    username, comment.getAuthor().getUsername(), comment.getId());
            });
        }
        
        return mentions;
    }
    
    /**
     * Update mentions when comment is edited.
     * 
     * Process:
     * 1. Delete all existing mentions for the comment
     * 2. Re-parse mentions from updated content
     * 3. Create new mention records
     * 
     * This ensures mentions stay in sync with comment content.
     * 
     * @param comment Comment that was edited
     * @return List of new mentions
     */
    @Transactional
    public List<Mention> updateMentions(Comment comment) {
        // Delete existing mentions
        mentionRepository.deleteByComment(comment);
        
        // Re-parse and create new mentions
        return processMentions(comment);
    }
    
    /**
     * Get all mentions for a user.
     * 
     * Used for displaying mentions in notification center.
     * 
     * @param user User to get mentions for
     * @return List of mentions for the user
     */
    @Transactional(readOnly = true)
    public List<Mention> getMentionsForUser(User user) {
        return mentionRepository.findByMentionedUser(user);
    }
    
    /**
     * Get all mentions in a comment.
     * 
     * Used for displaying who was mentioned in a comment.
     * 
     * @param comment Comment to get mentions from
     * @return List of mentions in the comment
     */
    @Transactional(readOnly = true)
    public List<Mention> getMentionsInComment(Comment comment) {
        return mentionRepository.findByComment(comment);
    }
}

