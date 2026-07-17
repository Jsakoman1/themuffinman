package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessProfileRequestDTO;
import com.themuffinman.app.business.service.BusinessProfileService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionBusinessProfileMutationExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessProfileService service;
    public VisionBusinessProfileMutationExecutionAdapter(BusinessProfileService service) { this.service = service; }
    @Override public String capabilityId() { return "create_business_profile"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        String name = conversation == null || conversation.getSlotData() == null ? null : conversation.getSlotData().get("business_name");
        if (conversation == null || conversation.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required.");
        if (name == null || name.isBlank()) return VisionExecutionResult.blocked("Business name is required.");
        service.saveMyProfile(BusinessProfileRequestDTO.builder().businessName(name).build(), conversation.getOwner());
        return VisionExecutionResult.executedAction(capabilityId());
    }
}
