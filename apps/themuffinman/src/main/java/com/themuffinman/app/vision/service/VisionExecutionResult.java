package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
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
    private String failureCode;
    private boolean retryable;
    private Quest createdQuest;
    private CircleGroupResponseDTO createdCircle;
    private CircleRequestResponseDTO circleRequest;
    private QuestApplicationResponseDTO application;
    private AppUserResponseDTO updatedProfile;
    private ThingListingResponseDTO createdThing;
    private ThingBorrowRequestResponseDTO borrowRequest;

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
                .failureCode(classifyFailure(blockingReason))
                .retryable(isRetryable(blockingReason))
                .build();
    }

    public static VisionExecutionResult blocked(String failureCode, String blockingReason, boolean retryable) {
        return VisionExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .failureCode(failureCode)
                .retryable(retryable)
                .build();
    }

    private static String classifyFailure(String reason) {
        String value = reason == null ? "" : reason.toLowerCase(java.util.Locale.ROOT);
        if (value.contains("disabled") || value.contains("configuration")) return "CONFIGURATION";
        if (value.contains("required") || value.contains("invalid") || value.contains("missing")) return "VALIDATION";
        if (value.contains("not ready") || value.contains("complete") || value.contains("blocked")) return "STATE";
        if (value.contains("not available") || value.contains("unavailable")) return "UNAVAILABLE";
        return "EXECUTION_FAILED";
    }

    private static boolean isRetryable(String reason) {
        return "EXECUTION_FAILED".equals(classifyFailure(reason)) || "UNAVAILABLE".equals(classifyFailure(reason));
    }

    public static VisionExecutionResult executedAction(String capabilityId) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .build();
    }

    public static VisionExecutionResult executedThing(String capabilityId, ThingListingResponseDTO createdThing) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .createdThing(createdThing)
                .build();
    }

    public static VisionExecutionResult executedBorrowRequest(String capabilityId, ThingBorrowRequestResponseDTO borrowRequest) {
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(capabilityId)
                .borrowRequest(borrowRequest)
                .build();
    }
}
