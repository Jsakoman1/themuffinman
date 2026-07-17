package com.themuffinman.app.workmarket.model;

public enum QuestStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_CONFIRMATION,
    PAUSED,
    COMPLETED,
    CANCELLED;

    public boolean isActiveForWorker() {
        return this == ASSIGNED || this == IN_PROGRESS || this == WAITING_CONFIRMATION;
    }
}
