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
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private AppUser sender;

    @Column(name = "message_body", length = 2000)
    private String messageBody;

    @Column(name = "image_data_url", columnDefinition = "TEXT")
    private String imageDataUrl;

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    @Column(name = "attachment_mime_type", length = 120)
    private String attachmentMimeType;

    @Column(name = "attachment_data_url", columnDefinition = "TEXT")
    private String attachmentDataUrl;

    @Column(name = "attachment_size_bytes")
    private Integer attachmentSizeBytes;

    @Column(name = "attachment_storage_provider", length = 40)
    private String attachmentStorageProvider;

    @Column(name = "attachment_storage_key", length = 500)
    private String attachmentStorageKey;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    @Column(name = "client_message_id", length = 80)
    private String clientMessageId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "seen_at")
    private Instant seenAt;
}
