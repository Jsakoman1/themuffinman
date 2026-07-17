package com.themuffinman.app.vision.service;

import com.themuffinman.app.things.dto.ThingBorrowRequestDTO;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionRequestBorrowExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "request_borrow";
    private final ThingSharingService thingSharingService;

    public VisionRequestBorrowExecutionAdapter(ThingSharingService thingSharingService) {
        this.thingSharingService = thingSharingService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to request a borrow.");
        }
        String rawListingId = conversation.getSlotData().get("thing_listing_id");
        if (rawListingId == null || rawListingId.isBlank()) {
            return VisionExecutionResult.blocked("A thing listing id is required.");
        }
        try {
            ThingBorrowRequestResponseDTO request = thingSharingService.requestBorrow(
                    Long.parseLong(rawListingId),
                    ThingBorrowRequestDTO.builder().message(conversation.getSlotData().get("borrow_message")).build(),
                    conversation.getOwner()
            );
            return VisionExecutionResult.executedBorrowRequest(CAPABILITY_ID, request);
        } catch (NumberFormatException exception) {
            return VisionExecutionResult.blocked("The thing listing id is invalid.");
        }
    }
}
