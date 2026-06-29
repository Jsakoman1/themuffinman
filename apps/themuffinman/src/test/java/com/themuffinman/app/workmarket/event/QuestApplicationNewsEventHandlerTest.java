package com.themuffinman.app.workmarket.event;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.service.QuestNewsService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QuestApplicationNewsEventHandlerTest {

    private final QuestNewsService questNewsService = mock(QuestNewsService.class);
    private final QuestApplicationNewsEventHandler handler = new QuestApplicationNewsEventHandler(questNewsService);

    @Test
    void mapsApplicationEventsToQuestNewsService() {
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser applicant = TestFixtures.user(2L, "applicant");
        Quest quest = TestFixtures.quest(10L, owner, QuestStatus.OPEN);
        QuestApplication application = TestFixtures.questApplication(20L, quest, applicant, QuestApplicationStatus.PENDING);

        handler.handle(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.CREATED, quest, application, applicant));
        handler.handle(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.UPDATED, quest, application, applicant));
        handler.handle(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.WITHDRAWN, quest, application, applicant));
        handler.handle(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.APPROVED, quest, application, owner));
        handler.handle(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.DECLINED, quest, application, owner));

        verify(questNewsService).notifyApplicationCreated(quest, application, applicant);
        verify(questNewsService).notifyApplicationUpdated(quest, application, applicant);
        verify(questNewsService).notifyApplicationWithdrawn(quest, application, applicant);
        verify(questNewsService).notifyApplicationApproved(quest, application, owner);
        verify(questNewsService).notifyApplicationDeclined(quest, application, owner);
    }
}
