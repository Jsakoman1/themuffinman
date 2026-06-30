package com.themuffinman.app.vision.testing;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VisionConversationTestBuilder {

    private final VisionConversation conversation;

    private VisionConversationTestBuilder(Long id, AppUser owner, VisionIntent intent) {
        this.conversation = new VisionConversation();
        this.conversation.setId(id);
        this.conversation.setOwner(owner);
        this.conversation.setIntent(intent);
        this.conversation.setStatus(VisionConversationStatus.ACTIVE);
        this.conversation.setSlotData(new LinkedHashMap<>());
    }

    public static VisionConversationTestBuilder createQuest(Long id, AppUser owner) {
        return new VisionConversationTestBuilder(id, owner, VisionIntent.CREATE_QUEST);
    }

    public VisionConversationTestBuilder requestedSlot(String requestedSlot) {
        conversation.setRequestedSlot(requestedSlot);
        return this;
    }

    public VisionConversationTestBuilder status(VisionConversationStatus status) {
        conversation.setStatus(status);
        return this;
    }

    public VisionConversationTestBuilder slot(String key, String value) {
        conversation.getSlotData().put(key, value);
        return this;
    }

    public VisionConversationTestBuilder slots(Map<String, String> slotData) {
        conversation.getSlotData().putAll(slotData);
        return this;
    }

    public VisionConversation build() {
        return conversation;
    }
}
