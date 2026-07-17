package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessGalleryImageRequestDTO;
import com.themuffinman.app.business.service.BusinessGalleryService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionUpdateGalleryImageExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessGalleryService service;
    public VisionUpdateGalleryImageExecutionAdapter(BusinessGalleryService service) { this.service = service; }
    public String capabilityId() { return "update_gallery_image"; }
    public VisionExecutionResult execute(VisionConversation c) {
        try {
            BusinessGalleryImageRequestDTO dto = new BusinessGalleryImageRequestDTO();
            dto.setImageUrl(c.getSlotData().get("gallery_image_url"));
            dto.setAltText(c.getSlotData().get("gallery_alt_text"));
            service.updateMyGalleryImage(Long.parseLong(c.getSlotData().get("gallery_image_id")), dto, c.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException e) { return VisionExecutionResult.blocked("The gallery image could not be updated."); }
    }
}
