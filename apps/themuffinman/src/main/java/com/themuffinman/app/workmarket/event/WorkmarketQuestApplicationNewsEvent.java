package com.themuffinman.app.workmarket.event;

import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;

import java.util.UUID;

public record WorkmarketQuestApplicationNewsEvent(
        Type type,
        Quest quest,
        QuestApplication application,
        AppUser actor,
        UUID eventId
) implements DomainEvent {

    public WorkmarketQuestApplicationNewsEvent(Type type, Quest quest, QuestApplication application, AppUser actor) {
        this(type, quest, application, actor, UUID.randomUUID());
    }

    public enum Type {
        CREATED,
        UPDATED,
        WITHDRAWN,
        APPROVED,
        DECLINED
    }
}
