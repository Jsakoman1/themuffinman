package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class VisionExecutionService {

    private final VisionSurfacePolicy visionSurfacePolicy;
    private final Map<String, VisionCapabilityExecutionAdapter> adaptersByCapabilityId;

    public VisionExecutionService(
            VisionSurfacePolicy visionSurfacePolicy,
            List<VisionCapabilityExecutionAdapter> executionAdapters
    ) {
        this.visionSurfacePolicy = visionSurfacePolicy;
        Map<String, VisionCapabilityExecutionAdapter> adaptersByCapabilityId = new LinkedHashMap<>();
        for (VisionCapabilityExecutionAdapter adapter : executionAdapters) {
            String capabilityId = adapter.capabilityId();
            if (capabilityId == null || capabilityId.isBlank()) {
                throw new IllegalStateException("Vision execution adapter returned a blank capability id.");
            }
            VisionCapabilityExecutionAdapter previous = adaptersByCapabilityId.put(capabilityId, adapter);
            if (previous != null) {
                throw new IllegalStateException("Duplicate Vision execution adapter registered for capability " + capabilityId);
            }
        }
        this.adaptersByCapabilityId = Map.copyOf(adaptersByCapabilityId);
    }

    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null) {
            return VisionExecutionResult.blocked("Conversation is required for execution.");
        }
        VisionIntent intent = conversation.getIntent();
        if (intent == null || intent == VisionIntent.UNSUPPORTED) {
            return VisionExecutionResult.blocked("Execution is only supported for typed capability conversations.");
        }
        if (conversation.getStatus() != VisionConversationStatus.REVIEW_READY) {
            return VisionExecutionResult.blocked("Conversation is not ready for execution.");
        }
        String capabilityId = intent.name().toLowerCase(Locale.ROOT);
        if (!visionSurfacePolicy.canExecuteCapability(capabilityId)) {
            return VisionExecutionResult.blocked("Execution is disabled by configuration.");
        }
        VisionCapabilityExecutionAdapter adapter = adaptersByCapabilityId.get(capabilityId);
        if (adapter == null) {
            return VisionExecutionResult.blocked("Execution is not available for " + capabilityId + ".");
        }
        return adapter.execute(conversation);
    }
}
