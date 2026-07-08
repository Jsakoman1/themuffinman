package com.themuffinman.app.chat.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "chat_conversation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_conversation_pair", columnNames = {"left_participant_id", "right_participant_id"})
})
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_participant_id")
    private AppUser leftParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_participant_id")
    private AppUser rightParticipant;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false, length = 48)
    private ChatConversationType conversationType = ChatConversationType.DIRECT;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", length = 48)
    private ChatConversationContextType contextType;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "title", length = 160)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_preview", length = 240)
    private String lastMessagePreview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_sender_id")
    private AppUser lastMessageSender;

    @Column(name = "last_message_has_image", nullable = false)
    private boolean lastMessageHasImage;

    @Column(name = "last_message_deleted", nullable = false)
    private boolean lastMessageDeleted;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatConversationParticipant> participants = new LinkedHashSet<>();
}
