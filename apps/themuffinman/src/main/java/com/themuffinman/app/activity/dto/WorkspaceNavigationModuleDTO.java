package com.themuffinman.app.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkspaceNavigationModuleDTO {
    /** Stable module identity; clients must not use the label as an identifier. */
    private String id;
    private String label;
    private String iconKey;
    private String route;
    private int order;
    private boolean visible;
    private long attentionCount;
    private long unreadCount;
    private String relevanceReason;
    private List<WorkspaceNavigationChildDTO> children;
}
