package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    // Find conversations between two users
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "(m.sender = :user1 AND m.recipient = :user2) OR " +
           "(m.sender = :user2 AND m.recipient = :user1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessageEntity> findConversationBetweenUsers(
        @Param("user1") User user1, 
        @Param("user2") User user2);
    
    // Find all messages where user is sender or recipient
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "m.sender = :user OR m.recipient = :user " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findMessagesForUser(@Param("user") User user);
    
    // Find unread messages for a user
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "m.recipient = :recipient AND m.isRead = false " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findUnreadMessagesForUser(
        @Param("recipient") User recipient);
    
    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE " +
           "m.recipient = :recipient AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("recipient") User recipient);
    
    // Find all messages for a user (non-paginated)
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "m.sender = :user OR m.recipient = :user " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findAllMessagesForUser(@Param("user") User user);
}
