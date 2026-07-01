package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.event.QuestApplicationNewsEvent;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateMyApplicationUseCase {

    private final QuestApplicationWorkflowSupport workflowSupport;
    private final QuestApplicationRepository questApplicationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public QuestApplication execute(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        QuestApplication application = workflowSupport.requirePendingMyApplication(questId, currentUser);
        workflowSupport.validateApplicationInput(dto, application.getQuest());
        application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
        application.setProposedPrice(dto.getProposedPrice());
        QuestApplication savedApplication = questApplicationRepository.save(application);
        domainEventPublisher.publish(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.UPDATED, savedApplication.getQuest(), savedApplication, currentUser));
        return savedApplication;
    }
}
