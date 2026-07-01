package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.service.VisionPresentationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestApplicationPresentationAssemblerTest {

    private QuestApplicationPresentationAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new QuestApplicationPresentationAssembler(new VisionPresentationHelper());
    }

    @Test
    void resolveApplicantActionsIsPendingOnly() {
        com.themuffinman.app.vision.model.QuestApplication application = new com.themuffinman.app.vision.model.QuestApplication();
        application.setStatus(QuestApplicationStatus.PENDING);

        assertEquals(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW), assembler.resolveApplicantActions(application));
    }

    @Test
    void withAllowedActionsBuildsCanonicalPresentationShape() {
        var dto = new com.themuffinman.app.vision.dto.QuestApplicationResponseDTO();
        dto.setStatus(QuestApplicationStatus.PENDING);
        dto.setQuestStatus(QuestStatus.OPEN);
        dto.setQuestAssigneeTarget(2);

        var result = assembler.withAllowedActions(dto, List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW));

        assertTrue(result.getPresentation().isCanEdit());
        assertTrue(result.getPresentation().isCanWithdraw());
        assertEquals("OPEN", result.getPresentation().getQuestStatusLabel());
    }
}
