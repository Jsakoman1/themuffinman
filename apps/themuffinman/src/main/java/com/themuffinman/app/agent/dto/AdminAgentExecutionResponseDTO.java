package com.themuffinman.app.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAgentExecutionResponseDTO {
    private String capabilityId;
    private boolean executionEnabled;
    private boolean executed;
    private boolean confirmationRequired;
    private String summary;
    private String targetUserLabel;
    private Long targetUserId;
    private Integer requestedCount;
    private Integer effectiveCount;
    private String topic;
    private List<String> questTitles;
    private List<Long> createdQuestIds;
    private List<String> warnings;
    private List<String> blockingReasons;
}
