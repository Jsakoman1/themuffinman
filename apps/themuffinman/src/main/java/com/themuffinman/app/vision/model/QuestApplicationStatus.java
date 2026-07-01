package com.themuffinman.app.vision.model;

public enum QuestApplicationStatus {
    PENDING,
    APPROVED,
    DECLINED,
    WITHDRAWN;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isActive() {
        return this == PENDING || this == APPROVED;
    }
}
