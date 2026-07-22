package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.CodedResponseStatusException;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketUpdateQuestUseCaseTest {

    @Mock
    private WorkmarketQuestExecutionPrimitiveService executionPrimitiveService;
    @Mock
    private WorkmarketQuestUpdateService updateService;

    @Test
    void staleResourceVersionReturnsTypedConflictBeforeApplyingChanges() {
        WorkmarketUpdateQuestUseCase useCase = new WorkmarketUpdateQuestUseCase(executionPrimitiveService, updateService);
        Quest quest = quest(12L, 4L);
        QuestRequestDTO request = request(3L);
        AppUser owner = new AppUser();

        when(executionPrimitiveService.resolveTargetForOwnerMutation(12L, owner)).thenReturn(quest);

        CodedResponseStatusException exception = assertThrows(CodedResponseStatusException.class,
                () -> useCase.execute(12L, request, owner));

        assertEquals("STALE_RESOURCE", exception.getCode());
        verify(updateService, never()).applyQuestUpdates(quest, request, owner);
    }

    @Test
    void updateRequiresResourceVersion() {
        WorkmarketUpdateQuestUseCase useCase = new WorkmarketUpdateQuestUseCase(executionPrimitiveService, updateService);
        Quest quest = quest(12L, 4L);
        QuestRequestDTO request = request(null);
        AppUser owner = new AppUser();

        when(executionPrimitiveService.resolveTargetForOwnerMutation(12L, owner)).thenReturn(quest);

        CodedResponseStatusException exception = assertThrows(CodedResponseStatusException.class,
                () -> useCase.execute(12L, request, owner));

        assertEquals("RESOURCE_VERSION_REQUIRED", exception.getCode());
        verify(updateService, never()).applyQuestUpdates(quest, request, owner);
    }

    private Quest quest(Long id, Long version) {
        Quest quest = new Quest();
        quest.setId(id);
        quest.setVersion(version);
        return quest;
    }

    private QuestRequestDTO request(Long resourceVersion) {
        return QuestRequestDTO.builder()
                .title("Updated quest")
                .description("Updated description")
                .awardAmount(BigDecimal.ZERO)
                .resourceVersion(resourceVersion)
                .build();
    }
}
