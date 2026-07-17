package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessGalleryImageRequestDTO;
import com.themuffinman.app.business.service.BusinessGalleryService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateGalleryImageExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessGalleryService service;
    public VisionCreateGalleryImageExecutionAdapter(BusinessGalleryService service) { this.service = service; }
    public String capabilityId() { return "create_gallery_image"; }
    public VisionExecutionResult execute(VisionConversation c) {
        try {
            BusinessGalleryImageRequestDTO dto = new BusinessGalleryImageRequestDTO();
            dto.setImageUrl(c.getSlotData().get("gallery_image_url"));
            dto.setAltText(c.getSlotData().get("gallery_alt_text"));
            service.createMyGalleryImage(dto, c.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException e) { return VisionExecutionResult.blocked("The gallery image could not be created."); }
    }
}
