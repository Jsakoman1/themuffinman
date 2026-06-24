package com.themuffinman.app.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleRequestResponseDTO {

    private Long id;

    private Long requesterId;
    private String requesterUsername;
    @Nullable
    private String requesterProfileDescription;
    @Nullable
    private String requesterProfileAvatarDataUrl;

    private Long recipientId;
    private String recipientUsername;
    @Nullable
    private String recipientProfileDescription;
    @Nullable
    private String recipientProfileAvatarDataUrl;

    private Long counterpartUserId;
    private String counterpartUsername;
    @Nullable
    private String counterpartProfileDescription;
    @Nullable
    private String counterpartProfileAvatarDataUrl;
    private String requestSummaryLabel;

    private Instant createdAt;
    @Nullable
    private Instant acceptedAt;
    @Nullable
    private Instant blockedAt;
}
