package com.sidequest.sidequest.model;

public enum QuestStatus {
    OPEN,
    ASSIGNED,
    WAITING_CONFIRMATION,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean isActiveForOwner() {
        return this == ASSIGNED || this == IN_PROGRESS;
    }

    public boolean isActiveForWorker() {
        return this == ASSIGNED || this == IN_PROGRESS || this == WAITING_CONFIRMATION;
    }

    public boolean isClosed() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isVisibleOwnerWork() {
        return this == OPEN || this == WAITING_CONFIRMATION;
    }
}
