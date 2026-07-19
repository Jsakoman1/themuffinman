package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.CodedResponseStatusException;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.WorkerReassignmentRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class WorkmarketWorkerManagementServiceTest {

    @Mock
    private WorkmarketQuestApplicationRepository applicationRepository;

    @Mock
    private WorkmarketQuestRepository questRepository;

    @Mock
    private WorkmarketQuestApplicationWorkflowSupport workflowSupport;

    @Mock
    private WorkmarketQuestWorkflowNotificationService notificationService;

    @Mock
    private WorkmarketQuestApplicationReadService applicationReadService;

    private WorkmarketWorkerManagementService service;

    @BeforeEach
    void setUp() {
        service = new WorkmarketWorkerManagementService(
                applicationRepository,
                questRepository,
                workflowSupport,
                notificationService,
                applicationReadService
        );
    }

    @Test
    void releaseWorkerReopensQuestWhenASlotBecomesAvailable() {
        AppUser owner = user(1L, "owner");
        Quest quest = quest(10L, QuestStatus.ASSIGNED, 1);
        QuestApplication worker = application(11L, quest, user(2L, "worker"), QuestApplicationStatus.APPROVED);
        QuestApplicationResponseDTO response = new QuestApplicationResponseDTO();

        manage(quest, owner);
        when(applicationRepository.findForQuestApplicationDetailByStatus(10L, 10L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(worker));
        when(applicationRepository.countByQuestIdAndStatus(10L, QuestApplicationStatus.APPROVED)).thenReturn(0L);
        when(applicationReadService.toApplicantResponse(worker)).thenReturn(response);

        QuestApplicationResponseDTO result = service.releaseWorker(10L, 10L, owner);

        assertEquals(response, result);
        assertEquals(QuestApplicationStatus.RELEASED, worker.getStatus());
        assertEquals(QuestStatus.OPEN, quest.getStatus());
        verify(notificationService).notifyWorkerReleased(quest, owner, worker);
        verify(questRepository).save(quest);
    }

    @Test
    void replacementKeepsQuestAssignedAndSwapsApprovedWorker() {
        AppUser owner = user(1L, "owner");
        Quest quest = quest(10L, QuestStatus.IN_PROGRESS, 1);
        QuestApplication current = application(11L, quest, user(2L, "old-worker"), QuestApplicationStatus.APPROVED);
        QuestApplication replacement = application(12L, quest, user(3L, "new-worker"), QuestApplicationStatus.PENDING);
        WorkerReassignmentRequestDTO request = new WorkerReassignmentRequestDTO();
        request.setReplacementApplicationId(12L);
        QuestApplicationResponseDTO response = new QuestApplicationResponseDTO();

        manage(quest, owner);
        when(applicationRepository.findForQuestApplicationDetailByStatus(11L, 10L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(current));
        when(applicationRepository.findForQuestApplicationDetailByStatus(12L, 10L, QuestApplicationStatus.PENDING))
                .thenReturn(Optional.of(replacement));
        when(applicationRepository.save(current)).thenReturn(current);
        when(applicationRepository.save(replacement)).thenReturn(replacement);
        when(applicationReadService.toApplicantResponse(replacement)).thenReturn(response);

        QuestApplicationResponseDTO result = service.replaceWorker(10L, 11L, request, owner);

        assertEquals(response, result);
        assertEquals(QuestApplicationStatus.RELEASED, current.getStatus());
        assertEquals(QuestApplicationStatus.APPROVED, replacement.getStatus());
        assertEquals(QuestStatus.ASSIGNED, quest.getStatus());
        verify(notificationService).notifyWorkerReassigned(quest, owner, current, replacement);
    }

    @Test
    void nonOwnerCannotManageWorkers() {
        AppUser viewer = user(4L, "viewer");
        Quest quest = quest(10L, QuestStatus.ASSIGNED, 1);
        when(workflowSupport.requireQuest(10L)).thenReturn(quest);
        when(workflowSupport.canManageWorkers(quest, viewer)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.releaseWorker(10L, 11L, viewer));
    }

    @Test
    void replacementRequiresPendingApplication() {
        AppUser owner = user(1L, "owner");
        Quest quest = quest(10L, QuestStatus.ASSIGNED, 1);
        QuestApplication current = application(11L, quest, user(2L, "old-worker"), QuestApplicationStatus.APPROVED);
        WorkerReassignmentRequestDTO request = new WorkerReassignmentRequestDTO();
        request.setReplacementApplicationId(12L);

        manage(quest, owner);
        when(applicationRepository.findForQuestApplicationDetailByStatus(11L, 10L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(current));
        when(applicationRepository.findForQuestApplicationDetailByStatus(12L, 10L, QuestApplicationStatus.PENDING))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.replaceWorker(10L, 11L, request, owner));
        assertEquals(409, exception.getStatusCode().value());
        assertEquals("REPLACEMENT_APPLICATION_STALE", ((CodedResponseStatusException) exception).getCode());
        assertEquals(QuestApplicationStatus.APPROVED, current.getStatus());
    }

    @Test
    void staleQuestLifecycleReturnsConflictBeforeReadingWorker() {
        AppUser owner = user(1L, "owner");
        Quest quest = quest(10L, QuestStatus.COMPLETED, 1);
        when(workflowSupport.requireQuest(10L)).thenReturn(quest);
        when(workflowSupport.canManageWorkers(quest, owner)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.releaseWorker(10L, 11L, owner));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("QUEST_WORKER_STATE_STALE", ((CodedResponseStatusException) exception).getCode());
    }

    private void manage(Quest quest, AppUser actor) {
        when(workflowSupport.requireQuest(quest.getId())).thenReturn(quest);
        when(workflowSupport.canManageWorkers(quest, actor)).thenReturn(true);
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
