package com.themuffinman.app.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter @Builder
public class ActivityItemDTO {
    /** Backend-owned source family. Clients render it but do not infer permissions or actions from it. */
    private String source;
    private String kind;
    private String title;
    private String summary;
    private String route;
    private String primaryActionLabel;
    private Instant occurredAt;
    private String resumeKey;
    private boolean resumable;
    private String deliveryState;
    private String readState;
    private boolean retryable;
}
