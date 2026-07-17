package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketQuestStateTransitionServiceTest {

    @Mock
    private WorkmarketQuestApplicationRepository questApplicationRepository;

    @Mock
    private WorkmarketQuestValidationService questValidationService;

    @Mock
    private WorkmarketQuestWorkflowNotificationService notificationService;

    @Mock
    private WorkmarketQuestAccessPolicyService accessPolicyService;

    @Test
    void ownerCanCancelActiveQuestAndNotifiesParticipants() {
        WorkmarketQuestStateTransitionService service = new WorkmarketQuestStateTransitionService(
                questApplicationRepository,
                questValidationService,
                notificationService,
                accessPolicyService
        );
        AppUser owner = new AppUser();
        Quest quest = new Quest();
        quest.setId(42L);
        quest.setTitle("Move a sofa");
        quest.setStatus(QuestStatus.IN_PROGRESS);
        when(accessPolicyService.isQuestOwner(quest, owner)).thenReturn(true);

        service.applyOwnerQuestStatusChange(quest, QuestStatus.CANCELLED, owner);

        assertEquals(QuestStatus.CANCELLED, quest.getStatus());
        verify(notificationService).notifyQuestCancelled(quest, owner);
    }

    @Test
    void nonOwnerCannotCancelQuest() {
        WorkmarketQuestStateTransitionService service = new WorkmarketQuestStateTransitionService(
                questApplicationRepository,
                questValidationService,
                notificationService,
                accessPolicyService
        );
        AppUser viewer = new AppUser();
        Quest quest = new Quest();
        quest.setStatus(QuestStatus.OPEN);
        when(accessPolicyService.isQuestOwner(quest, viewer)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> service.applyOwnerQuestStatusChange(quest, QuestStatus.CANCELLED, viewer));
        assertEquals(QuestStatus.OPEN, quest.getStatus());
    }

    @Test
    void completedQuestCannotBeCancelled() {
        WorkmarketQuestStateTransitionService service = new WorkmarketQuestStateTransitionService(
                questApplicationRepository,
                questValidationService,
                notificationService,
                accessPolicyService
        );
        AppUser owner = new AppUser();
        Quest quest = new Quest();
        quest.setStatus(QuestStatus.COMPLETED);
        when(accessPolicyService.isQuestOwner(quest, owner)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> service.applyOwnerQuestStatusChange(quest, QuestStatus.CANCELLED, owner));
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }

    @Test
    void ownerCanPauseAndResumeQuestToPreviousState() {
        WorkmarketQuestStateTransitionService service = new WorkmarketQuestStateTransitionService(
                questApplicationRepository, questValidationService, notificationService, accessPolicyService);
        AppUser owner = new AppUser();
        Quest quest = new Quest();
        quest.setId(43L);
        quest.setTitle("Paint a room");
        quest.setStatus(QuestStatus.IN_PROGRESS);
        when(accessPolicyService.isQuestOwner(quest, owner)).thenReturn(true);

        service.applyOwnerQuestStatusChange(quest, QuestStatus.PAUSED, owner);

        assertEquals(QuestStatus.PAUSED, quest.getStatus());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getPausedFromStatus());
        verify(notificationService).notifyQuestApplicants(
                quest, owner, QuestNewsType.QUEST_PAUSED, "Quest paused", "The quest \"Paint a room\" was paused by its owner.");

        service.applyOwnerQuestStatusChange(quest, QuestStatus.OPEN, owner);

        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertEquals(null, quest.getPausedFromStatus());
        verify(notificationService).notifyQuestApplicants(
                quest, owner, QuestNewsType.QUEST_RESUMED, "Quest resumed", "The quest \"Paint a room\" was resumed by its owner.");
    }

    @Test
    void nonOwnerCannotPauseQuest() {
        WorkmarketQuestStateTransitionService service = new WorkmarketQuestStateTransitionService(
                questApplicationRepository, questValidationService, notificationService, accessPolicyService);
        AppUser viewer = new AppUser();
        Quest quest = new Quest();
        quest.setStatus(QuestStatus.OPEN);
        when(accessPolicyService.isQuestOwner(quest, viewer)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> service.applyOwnerQuestStatusChange(quest, QuestStatus.PAUSED, viewer));
        assertEquals(QuestStatus.OPEN, quest.getStatus());
    }
}
