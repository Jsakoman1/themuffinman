package com.themuffinman.app.chat.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chat_conversation_member_state", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_conversation_member_state", columnNames = {"conversation_id", "user_id"})
})
public class ChatConversationMemberState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "muted_until")
    private Instant mutedUntil;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "last_opened_at")
    private Instant lastOpenedAt;

    @Column(name = "last_delivered_message_id")
    private Long lastDeliveredMessageId;

    @Column(name = "last_delivered_at")
    private Instant lastDeliveredAt;

    @Column(name = "last_seen_message_id")
    private Long lastSeenMessageId;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
}
