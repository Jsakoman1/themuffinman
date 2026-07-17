package com.themuffinman.app.vision.service;

import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionCancelBorrowExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private static final String CAPABILITY_ID = "cancel_borrow";
    private final ThingSharingService service;
    public VisionCancelBorrowExecutionAdapter(ThingSharingService service) { this.service = service; }
    @Override public String capabilityId() { return CAPABILITY_ID; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required.");
        try {
            ThingBorrowRequestResponseDTO result = service.cancelBorrowRequest(Long.parseLong(conversation.getSlotData().get("borrow_request_id")), conversation.getOwner());
            return VisionExecutionResult.executedBorrowRequest(CAPABILITY_ID, result);
        } catch (NumberFormatException exception) { return VisionExecutionResult.blocked("The borrow request id is invalid."); }
    }
}
