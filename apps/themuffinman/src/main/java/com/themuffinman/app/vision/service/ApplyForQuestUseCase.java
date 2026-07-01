package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.event.QuestApplicationNewsEvent;
import com.themuffinman.app.vision.mapper.QuestApplicationMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplyForQuestUseCase {

    private final QuestApplicationWorkflowSupport workflowSupport;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationMgr questApplicationMgr;
    private final DomainEventPublisher domainEventPublisher;

    public QuestApplication execute(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        Quest quest = workflowSupport.requireVisibleOpenQuest(questId, currentUser);
        workflowSupport.validateNotQuestCreator(quest, currentUser);
        workflowSupport.validateNoDuplicateApplication(questId, currentUser.getId());
        workflowSupport.validateApplicationInput(dto, quest);

        QuestApplication application = questApplicationMgr.toEntity(dto, quest, currentUser);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        domainEventPublisher.publish(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.CREATED, quest, savedApplication, currentUser));
        return savedApplication;
    }
}
