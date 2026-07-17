package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessOfferingService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionArchiveOfferingExecutionAdapterTest {
    @Test
    void archivesOwnedOfferingThroughBackendService() {
        BusinessOfferingService service = mock(BusinessOfferingService.class);
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setSlotData(new LinkedHashMap<>(Map.of("offering_id", "42")));
        assertTrue(new VisionArchiveOfferingExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).deleteMyOffering(42L, conversation.getOwner());
    }
}
