package com.themuffinman.app.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter @Builder
public class ActivityItemDTO {
    private String kind;
    private String title;
    private String summary;
    private String route;
    private Instant occurredAt;
    private String resumeKey;
    private boolean resumable;
}
