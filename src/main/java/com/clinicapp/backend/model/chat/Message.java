package com.clinicapp.backend.model.chat;

import com.clinicapp.backend.model.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "chat_messages_v2", indexes = {
    @Index(name = "idx_message_conversation", columnList = "conversation_id"),
    @Index(name = "idx_message_sender", columnList = "sender_id"),
    @Index(name = "idx_message_created", columnList = "created_at"),
    @Index(name = "idx_message_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"conversation", "readBy", "deletedBy"})
@ToString(exclude = {"conversation", "readBy", "deletedBy"})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "edit_history", columnDefinition = "TEXT")
    private String editHistory;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "message_read_receipts",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_read_message", columnList = "message_id"),
            @Index(name = "idx_read_user", columnList = "user_id")
        }
    )
    @Builder.Default
    private Set<User> readBy = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "message_deletions",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_deletion_message", columnList = "message_id"),
            @Index(name = "idx_deletion_user", columnList = "user_id")
        }
    )
    @Builder.Default
    private Set<User> deletedBy = new HashSet<>();

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM,
        NOTIFICATION
    }

    public enum MessageStatus {
        SENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    public void markAsReadBy(User user) {
        if (readBy == null) {
            readBy = new HashSet<>();
        }
        readBy.add(user);
        if (status != MessageStatus.READ && !user.equals(sender)) {
            status = MessageStatus.READ;
        }
    }

    public void markAsDeletedBy(User user) {
        if (deletedBy == null) {
            deletedBy = new HashSet<>();
        }
        deletedBy.add(user);
    }

    public boolean isDeletedFor(User user) {
        return deletedBy != null && deletedBy.contains(user);
    }

   
    public boolean isReadBy(User user) {
        return readBy != null && readBy.contains(user);
    }

   
    public void editContent(String newContent) {
        if (this.editHistory == null) {
            this.editHistory = "[{\"content\":\"" + this.content + "\",\"editedAt\":\"" + LocalDateTime.now() + "\"}]";
        } else {
            this.editHistory = this.editHistory.substring(0, this.editHistory.length() - 1) + 
                ",{\"content\":\"" + this.content + "\",\"editedAt\":\"" + LocalDateTime.now() + "\"}]";
        }
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }
}
