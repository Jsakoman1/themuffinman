package com.themuffinman.app.activity.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkspaceNavigationChildDTO {
    /** Stable route identity consumed by Web and future native clients. */
    private String id;
    private String label;
    private String route;
    private int order;
    private boolean visible;
    private long attentionCount;
    private long unreadCount;
    private String relevanceReason;
}
