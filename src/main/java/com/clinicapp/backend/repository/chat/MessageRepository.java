package com.clinicapp.backend.repository.chat;

import com.clinicapp.backend.model.chat.Conversation;
import com.clinicapp.backend.model.chat.Message;
import com.clinicapp.backend.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

   
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationForUser(@Param("conversation") Conversation conversation, 
                                            @Param("user") User user, 
                                            Pageable pageable);

    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "ORDER BY m.createdAt DESC")
    List<Message> findRecentByConversation(@Param("conversation") Conversation conversation,
                                           @Param("user") User user,
                                           Pageable pageable);

   
    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy")
    long countUnreadInConversation(@Param("conversation") Conversation conversation, 
                                   @Param("user") User user);

   
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.conversation c " +
           "JOIN c.participants p " +
           "WHERE p = :user " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy")
    long countTotalUnreadForUser(@Param("user") User user);

   
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "ORDER BY m.createdAt ASC")
    List<Message> findUnreadInConversation(@Param("conversation") Conversation conversation, 
                                           @Param("user") User user);

    
    @Modifying
    @Query("UPDATE Message m " +
           "SET m.status = 'READ' " +
           "WHERE m.conversation = :conversation " +
           "AND m.sender != :reader " +
           "AND :reader NOT MEMBER OF m.readBy")
    void markMessagesAsRead(@Param("conversation") Conversation conversation, 
                           @Param("reader") User reader);

    
    @Query("SELECT m FROM Message m " +
           "LEFT JOIN FETCH m.sender " +
           "LEFT JOIN FETCH m.conversation " +
           "LEFT JOIN FETCH m.readBy " +
           "WHERE m.id = :id")
    Optional<Message> findByIdWithDetails(@Param("id") Long id);

   
    @Query("SELECT m FROM Message m " +
           "WHERE m.sender = :sender " +
           "AND m.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY m.createdAt DESC")
    List<Message> findBySenderInTimeRange(@Param("sender") User sender,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

  
    @Modifying
    @Query(value = "INSERT INTO message_deletions (message_id, user_id) " +
           "SELECT m.id, :userId FROM chat_messages_v2 m " +
           "WHERE m.conversation_id IN (SELECT cp.conversation_id FROM conversation_participants cp WHERE cp.user_id = :userId) " +
           "AND m.created_at < :beforeDate " +
           "AND NOT EXISTS (SELECT 1 FROM message_deletions md WHERE md.message_id = m.id AND md.user_id = :userId)",
           nativeQuery = true)
    void softDeleteOldMessagesForUser(@Param("userId") Long userId,
                                      @Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND m.createdAt = (SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.conversation = :conversation)")
    Optional<Message> findLastMessageInConversation(@Param("conversation") Conversation conversation);

    // Idempotent insert of read receipts for a whole conversation
    @Modifying
    @Query(value = "INSERT INTO message_read_receipts (message_id, user_id) " +
           "SELECT m.id, :readerId FROM chat_messages_v2 m " +
           "WHERE m.conversation_id = :conversationId AND m.sender_id <> :readerId " +
           "ON CONFLICT (message_id, user_id) DO NOTHING",
           nativeQuery = true)
    void insertMissingReadReceipts(@Param("conversationId") Long conversationId,
                                   @Param("readerId") Long readerId);
}
