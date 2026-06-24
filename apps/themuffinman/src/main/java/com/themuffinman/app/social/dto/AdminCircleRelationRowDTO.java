package com.themuffinman.app.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCircleRelationRowDTO {
    private Long id;
    private String requesterUsername;
    private String recipientUsername;
    private String statusLabel;
    private String statusBadgeClass;
}
