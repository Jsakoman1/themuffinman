package com.themuffinman.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAttachmentUploadDTO {
    private Long uploadId;
    private String attachmentName;
    private String attachmentMimeType;
    private int attachmentSizeBytes;
    private String attachmentStorageProvider;
    private String attachmentStorageKey;
    private String attachmentUrl;
    private String attachmentUrlExpiresAt;
}
