package com.themuffinman.app.workmarket.event;

import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkmarketQuestApplicationNewsEventHandler {

    private final WorkmarketQuestNewsService questNewsService;

    @EventListener
    public void handle(WorkmarketQuestApplicationNewsEvent event) {
        switch (event.type()) {
            case CREATED -> questNewsService.notifyApplicationCreated(event.quest(), event.application(), event.actor());
            case UPDATED -> questNewsService.notifyApplicationUpdated(event.quest(), event.application(), event.actor());
            case WITHDRAWN -> questNewsService.notifyApplicationWithdrawn(event.quest(), event.application(), event.actor());
            case APPROVED -> questNewsService.notifyApplicationApproved(event.quest(), event.application(), event.actor());
            case DECLINED -> questNewsService.notifyApplicationDeclined(event.quest(), event.application(), event.actor());
        }
    }
}
