package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketQuestApplicationPresentationAssemblerTest {

    @Mock
    private WorkmarketPresentationHelper presentationHelper;

    @InjectMocks
    private WorkmarketQuestApplicationPresentationAssembler assembler;

    @Test
    void publishesCompleteManagementActionGuidanceForClients() {
        QuestApplicationResponseDTO dto = QuestApplicationResponseDTO.builder()
                .status(QuestApplicationStatus.PENDING)
                .questStatus(QuestStatus.OPEN)
                .build();
        when(presentationHelper.formatApplicationStatus(QuestApplicationStatus.PENDING)).thenReturn("Pending");
        when(presentationHelper.badgeClassForApplicationStatus(QuestApplicationStatus.PENDING)).thenReturn("pending");
        when(presentationHelper.surfaceClassForApplicationStatus(QuestApplicationStatus.PENDING)).thenReturn("pending");
        when(presentationHelper.formatQuestStatus(QuestStatus.OPEN)).thenReturn("Open");
        when(presentationHelper.badgeClassForQuestStatus(QuestStatus.OPEN)).thenReturn("open");

        QuestApplicationResponseDTO result = assembler.withAllowedActions(
                dto,
                List.of(ApplicationAllowedActionDTO.APPROVE, ApplicationAllowedActionDTO.DECLINE)
        );

        assertEquals("APPROVE", result.getActions().getFirst().getId());
        assertEquals("This person is assigned to the work and is notified.", result.getActions().getFirst().getOutcome());
        assertEquals("DECLINE", result.getActions().getLast().getId());
        assertEquals("This person is not selected and is notified.", result.getActions().getLast().getOutcome());
    }
}
