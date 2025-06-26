package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    // Find conversations between two users
    List<ChatMessageEntity> findBySenderAndRecipientOrRecipientAndSenderOrderByCreatedAtAsc(
        User sender1, User recipient1, User sender2, User recipient2);
    
    // Find all messages where user is sender or recipient
    Page<ChatMessageEntity> findBySenderOrRecipientOrderByCreatedAtDesc(User sender, User recipient, Pageable pageable);
    
    // Find unread messages for a user
    List<ChatMessageEntity> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);
    
    // Count unread messages for a user
    long countByRecipientAndIsReadFalse(User recipient);
    
    // Find all messages for a user
    List<ChatMessageEntity> findBySenderOrRecipientOrderByCreatedAtDesc(User sender, User recipient);
}
