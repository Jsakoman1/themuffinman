package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessGalleryService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionDeleteGalleryImageExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessGalleryService service;
    public VisionDeleteGalleryImageExecutionAdapter(BusinessGalleryService service) { this.service = service; }
    public String capabilityId() { return "delete_gallery_image"; }
    public VisionExecutionResult execute(VisionConversation c) {
        try {
            service.deleteMyGalleryImage(Long.parseLong(c.getSlotData().get("gallery_image_id")), c.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException e) { return VisionExecutionResult.blocked("The gallery image could not be deleted."); }
    }
}
