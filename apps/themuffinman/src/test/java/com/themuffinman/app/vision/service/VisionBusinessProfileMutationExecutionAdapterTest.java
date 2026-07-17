package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessProfileResponseDTO;
import com.themuffinman.app.business.service.BusinessProfileService;
import com.themuffinman.app.identity.model.AppUser;
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

class VisionBusinessProfileMutationExecutionAdapterTest {
    @Test
    void createsProfileThroughBackendService() {
        BusinessProfileService service = mock(BusinessProfileService.class);
        VisionConversation conversation = conversation("Acme Studio");
        when(service.saveMyProfile(any(), eq(conversation.getOwner()))).thenReturn(BusinessProfileResponseDTO.builder().build());
        assertTrue(new VisionBusinessProfileMutationExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).saveMyProfile(any(), eq(conversation.getOwner()));
    }

    @Test
    void updatesProfileThroughBackendService() {
        BusinessProfileService service = mock(BusinessProfileService.class);
        VisionConversation conversation = conversation("Acme Studio");
        when(service.updateMyBusinessNameForVision("Acme Studio", conversation.getOwner())).thenReturn(BusinessProfileResponseDTO.builder().build());
        assertTrue(new VisionBusinessProfileUpdateExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).updateMyBusinessNameForVision("Acme Studio", conversation.getOwner());
    }

    private VisionConversation conversation(String name) {
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setSlotData(new LinkedHashMap<>(Map.of("business_name", name)));
        return conversation;
    }
}
