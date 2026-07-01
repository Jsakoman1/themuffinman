package com.themuffinman.app.agent.runtime;

public interface AgentSurfacePolicy {

    AgentSurfaceId surfaceId();

    AgentSurfaceAuthority authority();

    boolean canExecuteCapability(String capabilityId);

    boolean allowsCrossUserTargeting();

    boolean requiresExplicitConfirmation(String capabilityId);
}
