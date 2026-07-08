package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
class VisionWorkmarketApplicationMutationAdapter {

    private final WorkmarketQuestApplicationService questApplicationService;

    QuestApplicationResponseDTO createApplication(
            AppUser currentUser,
            Long questId,
            String message,
            String proposedPrice
    ) {
        return questApplicationService.applyForQuest(
                questId,
                QuestApplicationRequestDTO.builder()
                        .message(message)
                        .proposedPrice(parsePrice(proposedPrice))
                        .build(),
                currentUser
        );
    }

    QuestApplicationResponseDTO updateApplication(
            AppUser currentUser,
            Long questId,
            String message,
            String proposedPrice
    ) {
        return questApplicationService.updateMyApplication(
                questId,
                QuestApplicationRequestDTO.builder()
                        .message(message)
                        .proposedPrice(parsePrice(proposedPrice))
                        .build(),
                currentUser
        );
    }

    QuestApplicationResponseDTO withdrawApplication(AppUser currentUser, Long questId) {
        return questApplicationService.withdrawMyApplication(questId, currentUser);
    }

    QuestApplicationResponseDTO approveManagedApplication(AppUser currentUser, Long questId, Long applicationId) {
        return questApplicationService.approveApplication(questId, applicationId, currentUser);
    }

    QuestApplicationResponseDTO declineManagedApplication(AppUser currentUser, Long questId, Long applicationId) {
        return questApplicationService.declineApplication(questId, applicationId, currentUser);
    }

    private BigDecimal parsePrice(String proposedPrice) {
        return proposedPrice != null && !proposedPrice.isBlank()
                ? new BigDecimal(proposedPrice.trim().replace(',', '.'))
                : null;
    }
}
