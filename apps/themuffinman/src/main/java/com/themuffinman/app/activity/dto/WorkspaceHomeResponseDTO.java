package com.themuffinman.app.activity.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class WorkspaceHomeResponseDTO {
    String contractVersion;
    String greetingName;
    List<LauncherAction> launcherActions;

    @Value
    @Builder
    public static class LauncherAction {
        String id;
        String label;
        String description;
        String route;
        String iconKey;
        String colourRole;
    }
}
