package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workmarketQuestApplicationAdminService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestApplicationAdminService {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestApplicationViewAssembler applicationViewAssembler;
    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketApproveApplicationUseCase approveApplicationUseCase;
    private final WorkmarketDeclineApplicationUseCase declineApplicationUseCase;

    @Transactional
    public QuestApplicationResponseDTO updateApplicationForAdmin(
            Long applicationId,
            AdminQuestApplicationUpdateRequestDTO dto,
            AppUser currentUser
    ) {
        workflowSupport.validateAdmin(currentUser);
        if (dto == null) {
            throw ServiceErrors.badRequest("Application update request is required");
        }

        QuestApplication application = questApplicationRepository.findForApplicationDetail(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        if (dto.getMessage() != null) {
            if (!RichTextInputValidator.hasContent(dto.getMessage())) {
                throw ServiceErrors.badRequest("Application message is required");
            }
            application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
        }

        if (dto.getProposedPrice() != null) {
            if (workflowSupport.isFreeQuest(application.getQuest())) {
                throw ServiceErrors.badRequest("Free quest applications cannot have a proposed price");
            }
            if (dto.getProposedPrice().compareTo(java.math.BigDecimal.valueOf(0.01)) < 0) {
                throw ServiceErrors.badRequest("Proposed price must be at least 0.01");
            }
            application.setProposedPrice(dto.getProposedPrice());
        }

        if (dto.getStatus() != null) {
            QuestApplicationStatus targetStatus = dto.getStatus();
            if (targetStatus != application.getStatus()) {
                applyAdminStatusUpdate(application, targetStatus, currentUser);
            }
        }

        return applicationViewAssembler.toApplicantResponse(questApplicationRepository.save(application));
    }

    @Transactional
    public void deleteApplicationForAdmin(Long applicationId, AppUser currentUser) {
        workflowSupport.validateAdmin(currentUser);
        QuestApplication application = questApplicationRepository.findForApplicationDetail(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        Quest quest = application.getQuest();
        if (application.getStatus() == QuestApplicationStatus.APPROVED) {
            if (quest.getStatus() == QuestStatus.ASSIGNED || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
                quest.setStatus(QuestStatus.OPEN);
                questRepository.save(quest);
            } else {
                throw ServiceErrors.badRequest("Approved applications can only be deleted while the quest is assigned or waiting confirmation");
            }
        }

        questApplicationRepository.delete(application);
    }

    private void applyAdminStatusUpdate(QuestApplication application, QuestApplicationStatus targetStatus, AppUser currentUser) {
        Quest quest = application.getQuest();
        if (targetStatus == QuestApplicationStatus.APPROVED) {
            if (application.getStatus() != QuestApplicationStatus.PENDING) {
                throw ServiceErrors.badRequest("Only pending applications can be approved");
            }

            approveApplicationUseCase.execute(quest.getId(), application.getId(), currentUser);
            return;
        }

        if (targetStatus == QuestApplicationStatus.DECLINED) {
            if (application.getStatus() != QuestApplicationStatus.PENDING) {
                throw ServiceErrors.badRequest("Only pending applications can be declined");
            }

            declineApplicationUseCase.execute(quest.getId(), application.getId(), currentUser);
            return;
        }

        if (targetStatus == QuestApplicationStatus.WITHDRAWN) {
            if (application.getStatus() != QuestApplicationStatus.PENDING) {
                throw ServiceErrors.badRequest("Only pending applications can be withdrawn");
            }

            application.setStatus(QuestApplicationStatus.WITHDRAWN);
            return;
        }

        throw ServiceErrors.badRequest("Applications cannot be moved back to pending");
    }
}
