package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionCreateThingExecutionAdapterTest {

    @Test
    void createsThingThroughTheBackendSharingService() {
        ThingSharingService sharingService = mock(ThingSharingService.class);
        VisionCreateThingExecutionAdapter adapter = new VisionCreateThingExecutionAdapter(sharingService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(Map.of("thing_title", "Camping tent")));
        when(sharingService.saveMyListing(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(owner)))
                .thenReturn(ThingListingResponseDTO.builder().build());

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        verify(sharingService).saveMyListing(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(owner));
    }
}
