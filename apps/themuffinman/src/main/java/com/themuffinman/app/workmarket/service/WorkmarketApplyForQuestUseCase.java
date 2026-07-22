package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestApplicationMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketApplyForQuestUseCase")
@RequiredArgsConstructor
public class WorkmarketApplyForQuestUseCase {

    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestApplicationMgr questApplicationMgr;
    private final WorkmarketApplicationNewsPublisher applicationNewsPublisher;

    public QuestApplication execute(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        Quest quest = workflowSupport.requireVisibleOpenQuest(questId, currentUser);
        workflowSupport.validateNotQuestCreator(quest, currentUser);
        workflowSupport.validateNoDuplicateApplication(questId, currentUser.getId());
        workflowSupport.validateApplicationInput(dto, quest);

        QuestApplication application = questApplicationMgr.toEntity(dto, quest, currentUser);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        applicationNewsPublisher.publish(WorkmarketQuestApplicationNewsEvent.Type.CREATED, savedApplication, currentUser);
        return savedApplication;
    }
}
