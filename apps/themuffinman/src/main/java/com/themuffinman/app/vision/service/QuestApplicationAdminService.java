package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import com.themuffinman.app.vision.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestApplicationAdminService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestRepository questRepository;
    private final QuestApplicationViewAssembler applicationViewAssembler;
    private final QuestApplicationWorkflowSupport workflowSupport;
    private final ApproveApplicationUseCase approveApplicationUseCase;
    private final DeclineApplicationUseCase declineApplicationUseCase;

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
            if (!com.themuffinman.app.common.validation.RichTextInputValidator.hasContent(dto.getMessage())) {
                throw ServiceErrors.badRequest("Application message is required");
            }
            application.setMessage(com.themuffinman.app.common.validation.RichTextInputValidator.sanitize(dto.getMessage()));
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

        if (dto.getStatus() != null && dto.getStatus() != application.getStatus()) {
            applyAdminStatusUpdate(application, dto.getStatus(), currentUser);
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
