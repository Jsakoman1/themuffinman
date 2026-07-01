package com.themuffinman.app.agent.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminSyntheticQuestExecutionPlan {
    private String capabilityId;
    private String targetUserQuery;
    private int requestedCount;
    private String topic;
    private List<String> blockingReasons;

    public boolean isExecutable() {
        return blockingReasons == null || blockingReasons.isEmpty();
    }
}
