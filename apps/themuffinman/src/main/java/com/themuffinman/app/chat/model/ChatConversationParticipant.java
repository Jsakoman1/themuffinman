package com.themuffinman.app.chat.model;

import com.themuffinman.app.identity.model.AppUser;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chat_conversation_participant", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_conversation_participant", columnNames = {"conversation_id", "user_id"})
})
public class ChatConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ChatConversationParticipantRole role = ChatConversationParticipantRole.MEMBER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id")
    private AppUser addedBy;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();
}
