package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionRequestBorrowExecutionAdapterTest {

    @Test
    void requestsBorrowThroughTheBackendSharingService() {
        ThingSharingService sharingService = mock(ThingSharingService.class);
        VisionRequestBorrowExecutionAdapter adapter = new VisionRequestBorrowExecutionAdapter(sharingService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(Map.of("thing_listing_id", "42")));
        when(sharingService.requestBorrow(eq(42L), any(), eq(owner))).thenReturn(ThingBorrowRequestResponseDTO.builder().build());

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        verify(sharingService).requestBorrow(eq(42L), any(), eq(owner));
    }
}
