package com.themuffinman.app.trust.dto;

import lombok.Data;

@Data
public class SafetyReportRequestDTO {
    private Long targetUserId;
    private String targetFamily;
    private Long targetId;
    private String reason;
}
