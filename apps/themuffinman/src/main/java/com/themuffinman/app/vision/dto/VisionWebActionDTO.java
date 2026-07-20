package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionWebActionDTO {
    private String contractVersion;
    private String action;
    private String routeKey;
    private String canonicalPath;
    private String entityFamily;
    private Long targetId;
    private boolean preview;
    private String focus;
    private Map<String, String> filters;
    private List<String> comparisonTargetIds;
    private String viewerSafeLabel;
    private String returnContext;
    private List<String> allowedActions;
    private boolean requiresConfirmation;
    private boolean ambiguous;
    private List<String> recoveryOptions;
}
