package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String requesterProfileDescription;
    private String requesterProfileAvatarDataUrl;

    private Long recipientId;
    private String recipientUsername;
    private String recipientProfileDescription;
    private String recipientProfileAvatarDataUrl;

    private Instant createdAt;
    private Instant acceptedAt;
    private Instant blockedAt;
}
