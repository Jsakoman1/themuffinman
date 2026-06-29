package com.themuffinman.app.workmarket.event;

import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;

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
