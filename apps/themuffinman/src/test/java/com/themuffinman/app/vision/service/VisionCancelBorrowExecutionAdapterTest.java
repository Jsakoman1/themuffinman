package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionCancelBorrowExecutionAdapterTest {
    @Test
    void cancelsBorrowRequestThroughBackendSharingService() {
        ThingSharingService service = mock(ThingSharingService.class);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(Map.of("borrow_request_id", "42")));
        when(service.cancelBorrowRequest(42L, owner)).thenReturn(ThingBorrowRequestResponseDTO.builder().build());

        VisionExecutionResult result = new VisionCancelBorrowExecutionAdapter(service).execute(conversation);

        assertTrue(result.isExecuted());
        verify(service).cancelBorrowRequest(42L, owner);
    }
}
