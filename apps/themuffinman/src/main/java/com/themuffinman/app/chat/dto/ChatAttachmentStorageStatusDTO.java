package com.themuffinman.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAttachmentStorageStatusDTO {
    private boolean enabled;
    private String provider;
    private String mode;
    private String endpoint;
    private String bucket;
    private String localBasePath;
}
