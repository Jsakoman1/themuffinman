package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.model.QuestApplication;

public interface WorkmarketApplicationNewsPublisher {

    void publish(WorkmarketQuestApplicationNewsEvent.Type type, QuestApplication application, AppUser actor);
}
