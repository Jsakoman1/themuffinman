package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionWorkspaceHandoffDTO {
    private String contractVersion;
    private String contextLabel;
    private String source;
    private String returnTo;
    private String explanation;
}
