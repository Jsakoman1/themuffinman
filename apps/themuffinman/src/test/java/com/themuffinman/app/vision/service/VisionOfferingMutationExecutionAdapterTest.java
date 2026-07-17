package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessOfferingResponseDTO;
import com.themuffinman.app.business.service.BusinessOfferingService;
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

class VisionOfferingMutationExecutionAdapterTest {
    @Test
    void createsOfferingThroughBackendService() {
        BusinessOfferingService service = mock(BusinessOfferingService.class);
        VisionConversation c = conversation(Map.of("offering_title", "Haircut"));
        when(service.createMyOffering(any(), eq(c.getOwner()))).thenReturn(BusinessOfferingResponseDTO.builder().build());
        assertTrue(new VisionCreateOfferingExecutionAdapter(service).execute(c).isExecuted());
        verify(service).createMyOffering(any(), eq(c.getOwner()));
    }

    @Test
    void updatesOfferingTitleThroughBackendService() {
        BusinessOfferingService service = mock(BusinessOfferingService.class);
        VisionConversation c = conversation(Map.of("offering_id", "42", "offering_title", "Haircut"));
        when(service.updateMyOfferingTitleForVision(42L, "Haircut", c.getOwner())).thenReturn(BusinessOfferingResponseDTO.builder().build());
        assertTrue(new VisionUpdateOfferingExecutionAdapter(service).execute(c).isExecuted());
        verify(service).updateMyOfferingTitleForVision(42L, "Haircut", c.getOwner());
    }

    private VisionConversation conversation(Map<String, String> slots) {
        VisionConversation c = new VisionConversation();
        c.setOwner(new AppUser());
        c.setSlotData(new LinkedHashMap<>(slots));
        return c;
    }
}
