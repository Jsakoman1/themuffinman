package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketApproveApplicationUseCaseTest {

    @Mock
    private WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    @Mock
    private WorkmarketQuestApplicationRepository applicationRepository;
    @Mock
    private WorkmarketQuestRepository questRepository;
    @Mock
    private WorkmarketApplicationNewsPublisher applicationNewsPublisher;

    @Test
    void fillingFinalSlotKeepsOtherPendingApplicantsAvailableForReplacement() {
        WorkmarketApproveApplicationUseCase useCase = new WorkmarketApproveApplicationUseCase(
                workflowSupport, applicationRepository, questRepository, applicationNewsPublisher
        );
        AppUser owner = user(1L, "owner");
        Quest quest = quest(10L, QuestStatus.OPEN, 1);
        QuestApplication approved = application(11L, quest, user(2L, "worker"), QuestApplicationStatus.PENDING);
        QuestApplication pendingReplacement = application(12L, quest, user(3L, "candidate"), QuestApplicationStatus.PENDING);

        when(workflowSupport.requireOpenQuest(10L)).thenReturn(quest);
        when(workflowSupport.requirePendingApplication(10L, 11L)).thenReturn(approved);
        when(applicationRepository.countByQuestIdAndStatus(10L, QuestApplicationStatus.APPROVED)).thenReturn(0L);
        when(applicationRepository.save(approved)).thenReturn(approved);

        QuestApplication result = useCase.execute(10L, 11L, owner);

        assertEquals(approved, result);
        assertEquals(QuestApplicationStatus.APPROVED, approved.getStatus());
        assertEquals(QuestApplicationStatus.PENDING, pendingReplacement.getStatus());
        assertEquals(QuestStatus.ASSIGNED, quest.getStatus());
        verify(applicationRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(applicationNewsPublisher).publish(com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent.Type.APPROVED, approved, owner);
    }

    private Quest quest(Long id, QuestStatus status, int target) {
        Quest quest = new Quest();
        quest.setId(id);
        quest.setTitle("Test quest");
        quest.setStatus(status);
        quest.setAssigneeTarget(target);
        return quest;
    }

    private QuestApplication application(Long id, Quest quest, AppUser applicant, QuestApplicationStatus status) {
        QuestApplication application = new QuestApplication();
        application.setId(id);
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setStatus(status);
        return application;
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
