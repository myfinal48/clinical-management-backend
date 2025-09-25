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
@Table(name = "chat_conversations", indexes = {
    @Index(name = "idx_conversation_created", columnList = "created_at"),
    @Index(name = "idx_conversation_updated", columnList = "updated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"participants", "messages"})
@ToString(exclude = {"participants", "messages"})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConversationType type = ConversationType.DIRECT;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_conv_participant_conv", columnList = "conversation_id"),
            @Index(name = "idx_conv_participant_user", columnList = "user_id")
        }
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_sender_id")
    private User lastMessageSender;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public enum ConversationType {
        DIRECT,
        GROUP
    }
    public void addParticipant(User user) {
        if (participants == null) {
            participants = new HashSet<>();
        }
        participants.add(user);
    }

    public void removeParticipant(User user) {
        if (participants != null) {
            participants.remove(user);
        }
    }

    public boolean hasParticipant(User user) {
        if (user == null || user.getId() == null) return false;
        return participants != null && participants.stream().anyMatch(u -> u.getId() != null && u.getId().equals(user.getId()));
    }

    public void updateLastMessage(Message message) {
        this.lastMessageAt = message.getCreatedAt();
        this.lastMessagePreview = message.getContent().length() > 255 
            ? message.getContent().substring(0, 252) + "..." 
            : message.getContent();
        this.lastMessageSender = message.getSender();
        this.updatedAt = LocalDateTime.now();
    }
}
