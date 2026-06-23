package com.sidequest.sidequest.mapper;

import com.sidequest.sidequest.dto.CircleRequestResponseDTO;
import com.sidequest.sidequest.model.CircleRequest;
import com.sidequest.sidequest.service.RichTextInputValidator;
import org.springframework.stereotype.Component;

@Component
public class CircleRequestMgr {

    public CircleRequestResponseDTO toDto(CircleRequest circleRequest) {
        if (circleRequest == null) {
            return null;
        }

        return CircleRequestResponseDTO.builder()
                .id(circleRequest.getId())
                .requesterId(circleRequest.getRequester().getId())
                .requesterUsername(circleRequest.getRequester().getUsername())
                .requesterProfileDescription(RichTextInputValidator.sanitize(circleRequest.getRequester().getProfileDescription()))
                .requesterProfileAvatarDataUrl(circleRequest.getRequester().getProfileAvatarDataUrl())
                .recipientId(circleRequest.getRecipient().getId())
                .recipientUsername(circleRequest.getRecipient().getUsername())
                .recipientProfileDescription(RichTextInputValidator.sanitize(circleRequest.getRecipient().getProfileDescription()))
                .recipientProfileAvatarDataUrl(circleRequest.getRecipient().getProfileAvatarDataUrl())
                .createdAt(circleRequest.getCreatedAt())
                .acceptedAt(circleRequest.getAcceptedAt())
                .blockedAt(circleRequest.getBlockedAt())
                .build();
    }
}
