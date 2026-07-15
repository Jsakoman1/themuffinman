package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionRuntimeContextDTO {
    private String inputType;
    private VisionDeviceRoleDTO deviceRole;
    private VisionAttentionStateDTO attentionState;
    private String sessionAnchor;
    private List<String> actionHints;
    private VisionRuntimeCueDTO audioCue;
    private VisionRuntimeCueDTO hapticCue;
    private boolean consentRequired;
    private String consentReason;
    private boolean resumeAvailable;
    private String resumeHint;
    private boolean watchFriendly;
    private String presentationArchetype;
    private String density;
    private String primaryActionLabel;
    private List<String> visibleFields;
}
