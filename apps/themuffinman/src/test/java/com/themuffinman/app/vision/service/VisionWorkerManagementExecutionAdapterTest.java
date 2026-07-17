package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketWorkerManagementService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionWorkerManagementExecutionAdapterTest {

    @Test
    void releaseAdapterDelegatesReviewedWorkerIdsToBackendService() {
        WorkmarketWorkerManagementService service = mock(WorkmarketWorkerManagementService.class);
        AppUser owner = new AppUser();
        owner.setId(1L);
        VisionConversation conversation = conversation(owner, "release");
        when(service.releaseWorker(10L, 20L, owner)).thenReturn(new QuestApplicationResponseDTO());

        VisionExecutionResult result = new VisionReleaseWorkerExecutionAdapter(service).execute(conversation);

        assertTrue(result.isExecuted());
    }

    @Test
    void replaceAdapterDelegatesReplacementApplicationToBackendService() {
        WorkmarketWorkerManagementService service = mock(WorkmarketWorkerManagementService.class);
        AppUser owner = new AppUser();
        owner.setId(1L);
        VisionConversation conversation = conversation(owner, "replace");
        when(service.replaceWorker(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.same(owner)))
                .thenReturn(new QuestApplicationResponseDTO());

        VisionExecutionResult result = new VisionReplaceWorkerExecutionAdapter(service).execute(conversation);

        assertTrue(result.isExecuted());
    }

    private VisionConversation conversation(AppUser owner, String action) {
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>());
        conversation.getSlotData().put("worker_quest_id", "10");
        conversation.getSlotData().put("worker_application_id", "20");
        if ("replace".equals(action)) conversation.getSlotData().put("replacement_application_id", "30");
        return conversation;
    }
}
