package com.themuffinman.app.vision.event;

import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;

public record QuestApplicationNewsEvent(
        Type type,
        Quest quest,
        QuestApplication application,
        AppUser actor
) implements DomainEvent {

    public enum Type {
        CREATED,
        UPDATED,
        WITHDRAWN,
        APPROVED,
        DECLINED
    }
}
