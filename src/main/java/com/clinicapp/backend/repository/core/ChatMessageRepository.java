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
    
    // Find conversations between two users (excluding soft-deleted)
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "((m.sender = :user1 AND m.recipient = :user2 AND " +
           "  ((:user1 = m.sender AND m.deletedBySender = false) OR (:user1 = m.recipient AND m.deletedByRecipient = false))) OR " +
           " (m.sender = :user2 AND m.recipient = :user1 AND " +
           "  ((:user1 = m.sender AND m.deletedBySender = false) OR (:user1 = m.recipient AND m.deletedByRecipient = false)))) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessageEntity> findConversationBetweenUsers(
        @Param("user1") User user1, 
        @Param("user2") User user2);
    
    // Find all messages where user is sender or recipient (excluding soft-deleted)
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "(m.sender = :user AND m.deletedBySender = false) OR " +
           "(m.recipient = :user AND m.deletedByRecipient = false) " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findMessagesForUser(@Param("user") User user);
    
    // Find unread messages for a user (excluding soft-deleted)
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "m.recipient = :recipient AND m.isRead = false AND m.deletedByRecipient = false " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findUnreadMessagesForUser(
        @Param("recipient") User recipient);
    
    // Count unread messages for a user (excluding soft-deleted)
    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE " +
           "m.recipient = :recipient AND m.isRead = false AND m.deletedByRecipient = false")
    long countUnreadMessagesForUser(@Param("recipient") User recipient);
    
    // Find all messages for a user (non-paginated, excluding soft-deleted)
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "(m.sender = :user AND m.deletedBySender = false) OR " +
           "(m.recipient = :user AND m.deletedByRecipient = false) " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findAllMessagesForUser(@Param("user") User user);
}
