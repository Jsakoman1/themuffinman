package com.themuffinman.app.vision.service;

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
    private String blockingReason;
    private Quest createdQuest;

    public static VisionExecutionResult executed(Quest createdQuest) {
        return VisionExecutionResult.builder()
                .executed(true)
                .createdQuest(createdQuest)
                .build();
    }

    public static VisionExecutionResult blocked(String blockingReason) {
        return VisionExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .build();
    }
}
