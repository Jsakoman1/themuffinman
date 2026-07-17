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
@Table(name = "chat_attachment_upload")
public class ChatAttachmentUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private AppUser uploadedBy;

    @Column(name = "storage_provider", nullable = false, length = 40)
    private String storageProvider;

    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(name = "attachment_name", nullable = false, length = 255)
    private String attachmentName;

    @Column(name = "attachment_mime_type", nullable = false, length = 120)
    private String attachmentMimeType;

    @Column(name = "attachment_size_bytes", nullable = false)
    private Integer attachmentSizeBytes;

    @Column(name = "consumed_message_id")
    private Long consumedMessageId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
