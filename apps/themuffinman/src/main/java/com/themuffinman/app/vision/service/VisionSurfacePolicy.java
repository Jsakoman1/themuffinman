package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.runtime.AgentSurfaceAuthority;
import com.themuffinman.app.agent.runtime.AgentSurfaceId;
import com.themuffinman.app.agent.runtime.AgentSurfacePolicy;
import com.themuffinman.app.config.VisionProperties;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class VisionSurfacePolicy implements AgentSurfacePolicy {

    private static final Set<String> EXECUTABLE_CAPABILITY_IDS = Set.of("create_quest", "create_circle");

    private final VisionProperties visionProperties;

    public VisionSurfacePolicy(VisionProperties visionProperties) {
        this.visionProperties = visionProperties;
    }

    @Override
    public AgentSurfaceId surfaceId() {
        return AgentSurfaceId.VISION;
    }

    @Override
    public AgentSurfaceAuthority authority() {
        return AgentSurfaceAuthority.USER_SCOPED;
    }

    @Override
    public boolean canExecuteCapability(String capabilityId) {
        return visionProperties.isExecutionEnabled() && supportedExecutionCapabilityIds().contains(capabilityId);
    }

    @Override
    public boolean allowsCrossUserTargeting() {
        return false;
    }

    @Override
    public boolean requiresExplicitConfirmation(String capabilityId) {
        return supportedExecutionCapabilityIds().contains(capabilityId);
    }

    public Set<String> supportedExecutionCapabilityIds() {
        return EXECUTABLE_CAPABILITY_IDS;
    }
}
