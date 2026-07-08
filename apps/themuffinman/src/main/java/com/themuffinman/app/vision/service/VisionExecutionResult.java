package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
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
    private CircleRequestResponseDTO circleRequest;
    private QuestApplicationResponseDTO application;
    private AppUserResponseDTO updatedProfile;

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

    public static VisionExecutionResult executedProfile(String capabilityId, AppUserResponseDTO updatedProfile) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .updatedProfile(updatedProfile)
                .build();
    }

    public static VisionExecutionResult executedCircleRequest(String capabilityId, CircleRequestResponseDTO circleRequest) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .circleRequest(circleRequest)
                .build();
    }

    public static VisionExecutionResult executedApplication(String capabilityId, QuestApplicationResponseDTO application) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .application(application)
                .build();
    }

    public static VisionExecutionResult blocked(String blockingReason) {
        return VisionExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .build();
    }
}
