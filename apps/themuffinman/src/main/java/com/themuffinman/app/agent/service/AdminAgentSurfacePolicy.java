package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.runtime.AgentSurfaceAuthority;
import com.themuffinman.app.agent.runtime.AgentSurfaceId;
import com.themuffinman.app.agent.runtime.AgentSurfacePolicy;
import com.themuffinman.app.config.AgentProperties;
import org.springframework.stereotype.Service;

@Service
public class AdminAgentSurfacePolicy implements AgentSurfacePolicy {

    public static final String SYNTHETIC_QUEST_BATCH_CAPABILITY = "generate_synthetic_quests_for_user";

    private final AgentProperties agentProperties;

    public AdminAgentSurfacePolicy(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
    }

    @Override
    public AgentSurfaceId surfaceId() {
        return AgentSurfaceId.ADMIN_PLAYGROUND;
    }

    @Override
    public AgentSurfaceAuthority authority() {
        return AgentSurfaceAuthority.ADMIN_SCOPED;
    }

    @Override
    public boolean canExecuteCapability(String capabilityId) {
        return agentProperties.isAdminExecutionEnabled()
                && SYNTHETIC_QUEST_BATCH_CAPABILITY.equals(capabilityId);
    }

    @Override
    public boolean allowsCrossUserTargeting() {
        return true;
    }

    @Override
    public boolean requiresExplicitConfirmation(String capabilityId) {
        return SYNTHETIC_QUEST_BATCH_CAPABILITY.equals(capabilityId);
    }
}
