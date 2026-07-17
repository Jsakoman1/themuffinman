package com.themuffinman.app.vision.service;
import com.themuffinman.app.business.dto.BusinessOfferingRequestDTO;
import com.themuffinman.app.business.service.BusinessOfferingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionCreateOfferingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessOfferingService service;
    public VisionCreateOfferingExecutionAdapter(BusinessOfferingService service) { this.service = service; }
    public String capabilityId() { return "create_offering"; }
    public VisionExecutionResult execute(VisionConversation c) { try { service.createMyOffering(BusinessOfferingRequestDTO.builder().title(c.getSlotData().get("offering_title")).build(), c.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); } catch (RuntimeException e) { return VisionExecutionResult.blocked("The offering could not be created."); } }
}
