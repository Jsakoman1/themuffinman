package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;

public interface VisionCapabilityExecutionAdapter {
    String capabilityId();

    VisionExecutionResult execute(VisionConversation conversation);
}
