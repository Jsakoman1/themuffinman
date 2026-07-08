package com.themuffinman.app.vision.service;

import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.workmarket.model.Quest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionExecutionResult {
    private boolean executed;
    private String capabilityId;
    private String blockingReason;
    private Quest createdQuest;
    private CircleGroupResponseDTO createdCircle;

    public static VisionExecutionResult executed(String capabilityId, Quest createdQuest) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .createdQuest(createdQuest)
                .build();
    }

    public static VisionExecutionResult executedCircle(String capabilityId, CircleGroupResponseDTO createdCircle) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .createdCircle(createdCircle)
                .build();
    }

    public static VisionExecutionResult blocked(String blockingReason) {
        return VisionExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .build();
    }
}
