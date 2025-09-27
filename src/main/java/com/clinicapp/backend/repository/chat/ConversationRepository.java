package com.clinicapp.backend.repository.chat;

import com.clinicapp.backend.model.chat.Conversation;
import com.clinicapp.backend.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;      
import java.util.Optional;


@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

   
    @Query("SELECT c FROM Conversation c " +
           "JOIN c.participants p1 " +
           "JOIN c.participants p2 " +
           "WHERE c.type = 'DIRECT' " +
           "AND p1 = :user1 " +
           "AND p2 = :user2 " +
           "AND c.isActive = true")
    Optional<Conversation> findDirectConversation(@Param("user1") User user1, @Param("user2") User user2);


    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.participants p " +
           "WHERE p = :user " +
           "AND c.isActive = true " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST, c.updatedAt DESC")
    Page<Conversation> findByParticipant(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.participants p " +
           "WHERE p = :user " +
           "AND c.isActive = true " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST, c.updatedAt DESC")
    List<Conversation> findByParticipant(@Param("user") User user);

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.participants " +
           "WHERE c.id = :id")
    Optional<Conversation> findByIdWithParticipants(@Param("id") Long id);

 
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Conversation c " +
           "JOIN c.participants p " +
           "WHERE c.id = :conversationId " +
           "AND p = :user")
    boolean isUserParticipant(@Param("conversationId") Long conversationId, @Param("user") User user);

    
    @Query("SELECT COUNT(DISTINCT c) FROM Conversation c " +
           "JOIN c.participants p " +
           "WHERE p = :user " +
           "AND c.isActive = true")
    long countByParticipant(@Param("user") User user);


    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.participants p " +
           "JOIN c.messages m " +
           "WHERE p = :user " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "AND c.isActive = true " +
           "ORDER BY m.createdAt DESC")
    List<Conversation> findConversationsWithUnreadMessages(@Param("user") User user);
}
