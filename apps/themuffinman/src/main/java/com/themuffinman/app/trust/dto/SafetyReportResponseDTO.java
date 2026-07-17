package com.themuffinman.app.trust.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter @Builder
public class SafetyReportResponseDTO {
    private Long id;
    private String targetFamily;
    private Long targetId;
    private String reason;
    private String status;
    private Instant createdAt;
}
