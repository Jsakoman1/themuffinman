package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.event.QuestApplicationNewsEvent;
import com.themuffinman.app.vision.mapper.QuestApplicationMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestApplicationUseCaseContractTest {

    @Mock
    private QuestApplicationWorkflowSupport workflowSupport;

    @Mock
    private QuestApplicationRepository questApplicationRepository;

    @Mock
    private QuestApplicationMgr questApplicationMgr;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Test
    void applyForQuestUseCaseResolvesVisibleQuestValidatesPersistsAndPublishes() {
        ApplyForQuestUseCase useCase = new ApplyForQuestUseCase(
                workflowSupport,
                questApplicationRepository,
                questApplicationMgr,
                domainEventPublisher
        );
        ContractFixture fixture = new ContractFixture();
        QuestApplication application = fixture.application();

        when(workflowSupport.requireVisibleOpenQuest(fixture.quest().getId(), fixture.actor())).thenReturn(fixture.quest());
        when(questApplicationMgr.toEntity(fixture.request(), fixture.quest(), fixture.actor())).thenReturn(application);
        when(questApplicationRepository.save(application)).thenReturn(application);

        QuestApplication result = useCase.execute(fixture.quest().getId(), fixture.request(), fixture.actor());

        assertSame(application, result);
        InOrder order = inOrder(workflowSupport, questApplicationMgr, questApplicationRepository, domainEventPublisher);
        order.verify(workflowSupport).requireVisibleOpenQuest(fixture.quest().getId(), fixture.actor());
        order.verify(workflowSupport).validateNotQuestCreator(fixture.quest(), fixture.actor());
        order.verify(workflowSupport).validateNoDuplicateApplication(fixture.quest().getId(), fixture.actor().getId());
        order.verify(workflowSupport).validateApplicationInput(fixture.request(), fixture.quest());
        order.verify(questApplicationMgr).toEntity(fixture.request(), fixture.quest(), fixture.actor());
        order.verify(questApplicationRepository).save(application);

        QuestApplicationNewsEvent event = capturePublishedEvent();
        assertEquals(QuestApplicationNewsEvent.Type.CREATED, event.type());
        assertSame(fixture.quest(), event.quest());
        assertSame(application, event.application());
        assertSame(fixture.actor(), event.actor());
    }

    @Test
    void updateMyApplicationUseCasePersistsSanitizedInputAndPublishes() {
        UpdateMyApplicationUseCase useCase = new UpdateMyApplicationUseCase(
                workflowSupport,
                questApplicationRepository,
                domainEventPublisher
        );
        ContractFixture fixture = new ContractFixture();
        QuestApplication application = fixture.application();

        when(workflowSupport.requirePendingMyApplication(fixture.quest().getId(), fixture.actor())).thenReturn(application);
        when(questApplicationRepository.save(application)).thenReturn(application);

        QuestApplication result = useCase.execute(fixture.quest().getId(), fixture.request(), fixture.actor());

        assertSame(application, result);
        assertEquals(fixture.request().getMessage(), application.getMessage());
        assertEquals(fixture.request().getProposedPrice(), application.getProposedPrice());
        InOrder order = inOrder(workflowSupport, questApplicationRepository, domainEventPublisher);
        order.verify(workflowSupport).requirePendingMyApplication(fixture.quest().getId(), fixture.actor());
        order.verify(workflowSupport).validateApplicationInput(fixture.request(), fixture.quest());
        order.verify(questApplicationRepository).save(application);

        QuestApplicationNewsEvent event = capturePublishedEvent();
        assertEquals(QuestApplicationNewsEvent.Type.UPDATED, event.type());
        assertSame(fixture.quest(), event.quest());
        assertSame(application, event.application());
        assertSame(fixture.actor(), event.actor());
    }

    @Test
    void updateMyApplicationUseCaseFailsClosedWhenPendingApplicationResolutionFails() {
        UpdateMyApplicationUseCase useCase = new UpdateMyApplicationUseCase(
                workflowSupport,
                questApplicationRepository,
                domainEventPublisher
        );
        ContractFixture fixture = new ContractFixture();
        ResponseStatusException failure = new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Quest application not found for current user");
        when(workflowSupport.requirePendingMyApplication(fixture.quest().getId(), fixture.actor())).thenThrow(failure);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> useCase.execute(fixture.quest().getId(), fixture.request(), fixture.actor()));

        assertSame(failure, exception);
        verify(questApplicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(domainEventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void withdrawMyApplicationUseCaseMarksWithdrawnPersistsAndPublishes() {
        WithdrawMyApplicationUseCase useCase = new WithdrawMyApplicationUseCase(
                workflowSupport,
                questApplicationRepository,
                domainEventPublisher
        );
        ContractFixture fixture = new ContractFixture();
        QuestApplication application = fixture.application();

        when(workflowSupport.requirePendingMyApplication(fixture.quest().getId(), fixture.actor())).thenReturn(application);
        when(questApplicationRepository.save(application)).thenReturn(application);

        QuestApplication result = useCase.execute(fixture.quest().getId(), fixture.actor());

        assertSame(application, result);
        assertEquals(QuestApplicationStatus.WITHDRAWN, application.getStatus());
        InOrder order = inOrder(workflowSupport, questApplicationRepository, domainEventPublisher);
        order.verify(workflowSupport).requirePendingMyApplication(fixture.quest().getId(), fixture.actor());
        order.verify(questApplicationRepository).save(application);

        QuestApplicationNewsEvent event = capturePublishedEvent();
        assertEquals(QuestApplicationNewsEvent.Type.WITHDRAWN, event.type());
        assertSame(fixture.quest(), event.quest());
        assertSame(application, event.application());
        assertSame(fixture.actor(), event.actor());
    }

    private QuestApplicationNewsEvent capturePublishedEvent() {
        ArgumentCaptor<QuestApplicationNewsEvent> eventCaptor = ArgumentCaptor.forClass(QuestApplicationNewsEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        return eventCaptor.getValue();
    }

    private static class ContractFixture {
        private final AppUser actor;
        private final AppUser creator;
        private final Quest quest;
        private final QuestApplicationRequestDTO request;

        private ContractFixture() {
            actor = new AppUser();
            actor.setId(7L);
            actor.setUsername("worker");

            creator = new AppUser();
            creator.setId(3L);
            creator.setUsername("owner");

            quest = new Quest();
            quest.setId(12L);
            quest.setCreator(creator);
            quest.setTitle("Fence repair");
            quest.setAwardAmount(BigDecimal.valueOf(50));
            quest.setStatus(QuestStatus.OPEN);

            request = QuestApplicationRequestDTO.builder()
                    .message("I can help with the fence.")
                    .proposedPrice(BigDecimal.valueOf(45))
                    .build();
        }

        private AppUser actor() {
            return actor;
        }

        private Quest quest() {
            return quest;
        }

        private QuestApplicationRequestDTO request() {
            return request;
        }

        private QuestApplication application() {
            QuestApplication application = new QuestApplication();
            application.setId(21L);
            application.setQuest(quest);
            application.setApplicant(actor);
            application.setStatus(QuestApplicationStatus.PENDING);
            application.setMessage("Original message");
            application.setProposedPrice(BigDecimal.valueOf(40));
            return application;
        }
    }
}
