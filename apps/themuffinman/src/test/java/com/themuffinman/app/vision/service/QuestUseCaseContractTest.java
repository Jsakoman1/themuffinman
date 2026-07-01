package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.mapper.QuestMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestNewsType;
import com.themuffinman.app.vision.model.QuestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestUseCaseContractTest {

    @Mock
    private QuestExecutionPrimitiveService primitiveService;

    @Mock
    private QuestValidationService questValidationService;

    @Mock
    private QuestStateTransitionService questStateTransitionService;

    @Mock
    private QuestMgr questMgr;

    @Mock
    private LocationSettingsService locationSettingsService;

    @Mock
    private QuestUpdateService questUpdateService;

    @Test
    void createQuestUseCaseResolvesCreatorAndPersistsQuest() {
        CreateQuestUseCase useCase = new CreateQuestUseCase(
                questValidationService,
                questStateTransitionService,
                primitiveService,
                questMgr,
                locationSettingsService
        );
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();

        when(primitiveService.resolveCreator(fixture.request(), fixture.actor())).thenReturn(fixture.actor());
        when(questMgr.toEntity(fixture.request(), fixture.actor())).thenReturn(quest);
        when(primitiveService.persistMutation(quest)).thenReturn(quest);

        Quest result = useCase.execute(fixture.request(), fixture.actor());

        assertSame(quest, result);
        InOrder order = inOrder(questValidationService, primitiveService, questMgr, questStateTransitionService, locationSettingsService);
        order.verify(questValidationService).validateCreateRequest(fixture.request());
        order.verify(primitiveService).resolveCreator(fixture.request(), fixture.actor());
        order.verify(questMgr).toEntity(fixture.request(), fixture.actor());
        order.verify(questStateTransitionService).applyConfirmedQuestTermFields(quest, fixture.request().getScheduledAt(), fixture.request().getEndsAt(), fixture.request().getTermFixed());
        order.verify(locationSettingsService).applyQuestLocation(quest, fixture.request(), fixture.actor());
        verify(primitiveService).persistMutation(quest);
    }

    @Test
    void createQuestUseCaseFailsClosedWhenValidationRejectsPayload() {
        CreateQuestUseCase useCase = new CreateQuestUseCase(
                questValidationService,
                questStateTransitionService,
                primitiveService,
                questMgr,
                locationSettingsService
        );
        ContractFixture fixture = new ContractFixture();
        ResponseStatusException failure = new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Quest title is required");
        org.mockito.Mockito.doThrow(failure).when(questValidationService).validateCreateRequest(fixture.request());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> useCase.execute(fixture.request(), fixture.actor()));

        assertSame(failure, exception);
        verify(primitiveService, never()).persistMutation(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateQuestUseCaseResolvesTargetThenPersistsMutation() {
        UpdateQuestUseCase useCase = new UpdateQuestUseCase(primitiveService, questUpdateService);
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();

        when(primitiveService.resolveTargetForOwnerMutation(quest.getId(), fixture.actor())).thenReturn(quest);
        when(primitiveService.persistMutation(quest)).thenReturn(quest);

        Quest result = useCase.execute(quest.getId(), fixture.request(), fixture.actor());

        assertSame(quest, result);
        InOrder order = inOrder(primitiveService, questUpdateService);
        order.verify(primitiveService).resolveTargetForOwnerMutation(quest.getId(), fixture.actor());
        order.verify(questUpdateService).applyQuestUpdates(quest, fixture.request(), fixture.actor());
        order.verify(primitiveService).persistMutation(quest);
    }

    @Test
    void deleteQuestUseCaseRequiresOwnerResolutionBeforeDeletion() {
        DeleteQuestUseCase useCase = new DeleteQuestUseCase(primitiveService);
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();
        when(primitiveService.resolveTargetForOwnerMutation(quest.getId(), fixture.actor())).thenReturn(quest);

        useCase.execute(quest.getId(), fixture.actor());

        InOrder order = inOrder(primitiveService);
        order.verify(primitiveService).resolveTargetForOwnerMutation(quest.getId(), fixture.actor());
        order.verify(primitiveService).deleteMutationData(quest, fixture.actor());
    }

    @Test
    void startQuestUseCaseValidatesAuthorityStatePersistenceAndNotification() {
        StartQuestUseCase useCase = new StartQuestUseCase(primitiveService);
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();
        quest.setStatus(QuestStatus.ASSIGNED);
        when(primitiveService.resolveTargetForExecutionMutation(quest.getId(), fixture.actor())).thenReturn(quest);
        when(primitiveService.persistMutation(quest)).thenReturn(quest);

        Quest result = useCase.execute(quest.getId(), fixture.actor());

        assertSame(quest, result);
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        InOrder order = inOrder(primitiveService);
        order.verify(primitiveService).resolveTargetForExecutionMutation(quest.getId(), fixture.actor());
        order.verify(primitiveService).validateState(quest, QuestStatus.ASSIGNED, "Quest can only be started after an application is approved");
        order.verify(primitiveService).persistMutation(quest);
        order.verify(primitiveService).emitApprovedApplicantNotification(
                quest,
                fixture.actor(),
                QuestNewsType.QUEST_STARTED,
                "Quest started",
                "The quest \"" + quest.getTitle() + "\" has started.");
    }

    @Test
    void completeQuestUseCaseFailsClosedWhenStateGateRejectsExecution() {
        CompleteQuestUseCase useCase = new CompleteQuestUseCase(primitiveService);
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();
        ResponseStatusException failure = new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Quest can only be completed while it is in progress");
        when(primitiveService.resolveTargetForExecutionMutation(quest.getId(), fixture.actor())).thenReturn(quest);
        org.mockito.Mockito.doThrow(failure).when(primitiveService).validateState(quest, QuestStatus.IN_PROGRESS, "Quest can only be completed while it is in progress");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> useCase.execute(quest.getId(), fixture.actor()));

        assertSame(failure, exception);
        verify(primitiveService, never()).persistMutation(quest);
        verify(primitiveService, never()).emitApprovedApplicantNotification(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void confirmQuestTermChangeUseCasePersistsAndNotifiesBothActors() {
        ConfirmQuestTermChangeUseCase useCase = new ConfirmQuestTermChangeUseCase(primitiveService, questStateTransitionService);
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
        when(primitiveService.resolveTargetForTermDecision(quest.getId(), fixture.actor())).thenReturn(quest);
        when(primitiveService.persistMutation(quest)).thenReturn(quest);

        Quest result = useCase.execute(quest.getId(), fixture.actor());

        assertSame(quest, result);
        InOrder order = inOrder(primitiveService, questStateTransitionService);
        order.verify(primitiveService).resolveTargetForTermDecision(quest.getId(), fixture.actor());
        order.verify(questStateTransitionService).confirmQuestTermChange(quest);
        order.verify(primitiveService).persistMutation(quest);
        order.verify(primitiveService).emitApprovedApplicantNotification(
                quest,
                fixture.actor(),
                QuestNewsType.QUEST_TERM_CONFIRMED,
                "Quest time confirmed",
                "The new time for \"" + quest.getTitle() + "\" was confirmed.");
        order.verify(primitiveService).emitCreatorNotification(
                quest,
                fixture.actor(),
                QuestNewsType.QUEST_TERM_CONFIRMED,
                "Quest time confirmed",
                "The new time for \"" + quest.getTitle() + "\" was confirmed.");
    }

    @Test
    void rejectQuestTermChangeUseCasePersistsAndNotifiesBothActors() {
        RejectQuestTermChangeUseCase useCase = new RejectQuestTermChangeUseCase(primitiveService, questStateTransitionService);
        ContractFixture fixture = new ContractFixture();
        Quest quest = fixture.quest();
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
        when(primitiveService.resolveTargetForTermDecision(quest.getId(), fixture.actor())).thenReturn(quest);
        when(primitiveService.persistMutation(quest)).thenReturn(quest);

        Quest result = useCase.execute(quest.getId(), fixture.actor());

        assertSame(quest, result);
        InOrder order = inOrder(primitiveService, questStateTransitionService);
        order.verify(primitiveService).resolveTargetForTermDecision(quest.getId(), fixture.actor());
        order.verify(questStateTransitionService).rejectQuestTermChange(quest);
        order.verify(primitiveService).persistMutation(quest);
        order.verify(primitiveService).emitApprovedApplicantNotification(
                quest,
                fixture.actor(),
                QuestNewsType.QUEST_TERM_REJECTED,
                "Quest time rejected",
                "The proposed time change for \"" + quest.getTitle() + "\" was rejected.");
        order.verify(primitiveService).emitCreatorNotification(
                quest,
                fixture.actor(),
                QuestNewsType.QUEST_TERM_REJECTED,
                "Quest time rejected",
                "The proposed time change for \"" + quest.getTitle() + "\" was rejected.");
    }

    private static class ContractFixture {
        private final AppUser actor;
        private final QuestRequestDTO request;

        private ContractFixture() {
            actor = new AppUser();
            actor.setId(7L);
            actor.setUsername("owner");
            request = QuestRequestDTO.builder()
                    .title("Fence repair")
                    .description("Need help fixing the fence")
                    .awardAmount(BigDecimal.valueOf(50))
                    .scheduledAt(Instant.parse("2026-07-01T10:00:00Z"))
                    .endsAt(Instant.parse("2026-07-01T12:00:00Z"))
                    .termFixed(true)
                    .build();
        }

        private AppUser actor() {
            return actor;
        }

        private QuestRequestDTO request() {
            return request;
        }

        private Quest quest() {
            Quest quest = new Quest();
            quest.setId(12L);
            quest.setCreator(actor);
            quest.setTitle(request.getTitle());
            quest.setStatus(QuestStatus.OPEN);
            return quest;
        }
    }
}
