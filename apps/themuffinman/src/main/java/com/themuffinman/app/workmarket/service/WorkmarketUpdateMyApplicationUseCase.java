package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketUpdateMyApplicationUseCase")
@RequiredArgsConstructor
public class WorkmarketUpdateMyApplicationUseCase {

    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketApplicationNewsPublisher applicationNewsPublisher;

    public QuestApplication execute(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        QuestApplication application = workflowSupport.requirePendingMyApplication(questId, currentUser);
        workflowSupport.validateApplicationInput(dto, application.getQuest());
        application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
        application.setProposedPrice(dto.getProposedPrice());
        QuestApplication savedApplication = questApplicationRepository.save(application);
        applicationNewsPublisher.publish(WorkmarketQuestApplicationNewsEvent.Type.UPDATED, savedApplication, currentUser);
        return savedApplication;
    }
}
