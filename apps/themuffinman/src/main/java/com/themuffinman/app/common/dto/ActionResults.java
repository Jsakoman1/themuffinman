package com.themuffinman.app.common.dto;

public final class ActionResults {

    private ActionResults() {
    }

    public static ActionResultDTO of(String action, String message) {
        return ActionResultDTO.builder()
                .action(action)
                .message(message)
                .build();
    }
}
