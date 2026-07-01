package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.runtime.AgentSurfaceAuthority;
import com.themuffinman.app.agent.runtime.AgentSurfaceId;
import com.themuffinman.app.agent.runtime.AgentSurfacePolicy;
import com.themuffinman.app.config.VisionProperties;
import org.springframework.stereotype.Service;

@Service
public class VisionSurfacePolicy implements AgentSurfacePolicy {

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
        return visionProperties.isExecutionEnabled()
                && "create_quest".equals(capabilityId);
    }

    @Override
    public boolean allowsCrossUserTargeting() {
        return false;
    }

    @Override
    public boolean requiresExplicitConfirmation(String capabilityId) {
        return "create_quest".equals(capabilityId);
    }
}
