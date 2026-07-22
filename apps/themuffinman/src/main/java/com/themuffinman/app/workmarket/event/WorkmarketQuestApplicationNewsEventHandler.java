package com.themuffinman.app.workmarket.event;

import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class WorkmarketQuestApplicationNewsEventHandler {

    private final WorkmarketQuestNewsService questNewsService;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final AppUserRepository appUserRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(WorkmarketQuestApplicationNewsEvent event) {
        QuestApplication application = questApplicationRepository.findByIdDetailed(event.application().getId())
                .orElse(null);
        AppUser actor = appUserRepository.findById(event.actor().getId()).orElse(event.actor());
        if (application == null) {
            return;
        }
        switch (event.type()) {
            case CREATED -> questNewsService.notifyApplicationCreated(application.getQuest(), application, actor, event.eventId());
            case UPDATED -> questNewsService.notifyApplicationUpdated(application.getQuest(), application, actor, event.eventId());
            case WITHDRAWN -> questNewsService.notifyApplicationWithdrawn(application.getQuest(), application, actor, event.eventId());
            case APPROVED -> questNewsService.notifyApplicationApproved(application.getQuest(), application, actor, event.eventId());
            case DECLINED -> questNewsService.notifyApplicationDeclined(application.getQuest(), application, actor, event.eventId());
        }
    }
}
