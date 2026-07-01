package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.mapper.QuestApplicationMgr;
import com.themuffinman.app.vision.model.QuestApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestApplicationViewAssembler {

    private final QuestApplicationMgr questApplicationMgr;
    private final QuestApplicationPresentationAssembler questApplicationPresentationAssembler;

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return questApplicationPresentationAssembler.withAllowedActions(
                questApplicationMgr.toDto(application),
                questApplicationPresentationAssembler.resolveApplicantActions(application)
        );
    }

    public QuestApplicationResponseDTO toManagementResponse(QuestApplication application) {
        return questApplicationPresentationAssembler.withAllowedActions(
                questApplicationMgr.toDto(application),
                questApplicationPresentationAssembler.resolveManagementActions(application)
        );
    }

    public QuestApplicationResponseDTO toPublicResponse(QuestApplication application) {
        return questApplicationPresentationAssembler.withAllowedActions(questApplicationMgr.toDto(application), java.util.List.of());
    }

    public QuestApplicationResponseDTO toViewerResponse(QuestApplication application, AppUser currentUser) {
        if (application == null) {
            return null;
        }

        if (currentUser != null && application.getApplicant() != null && currentUser.getId().equals(application.getApplicant().getId())) {
            return toApplicantResponse(application);
        }

        return toPublicResponse(application);
    }
}
