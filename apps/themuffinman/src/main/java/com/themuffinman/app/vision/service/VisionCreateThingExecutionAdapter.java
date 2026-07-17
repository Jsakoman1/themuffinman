package com.themuffinman.app.vision.service;

import com.themuffinman.app.things.dto.ThingListingRequestDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateThingExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "create_thing";

    private final ThingSharingService thingSharingService;

    public VisionCreateThingExecutionAdapter(ThingSharingService thingSharingService) {
        this.thingSharingService = thingSharingService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to create a thing listing.");
        }
        String title = conversation.getSlotData().get("thing_title");
        if (title == null || title.isBlank()) {
            return VisionExecutionResult.blocked("A thing title is required.");
        }
        ThingListingRequestDTO request = ThingListingRequestDTO.builder()
                .title(title)
                .description(conversation.getSlotData().get("thing_description"))
                .conditionNote(conversation.getSlotData().get("thing_condition"))
                .available(true)
                .build();
        ThingListingResponseDTO created = thingSharingService.saveMyListing(request, conversation.getOwner());
        return VisionExecutionResult.executedThing(CAPABILITY_ID, created);
    }
}
