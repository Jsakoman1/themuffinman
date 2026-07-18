package com.themuffinman.app.things.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThingPreviewReadServiceTest {

    @Test
    void reusesViewerScopedListingDetailForPreview() {
        ThingSharingService sharingService = mock(ThingSharingService.class);
        AppUser viewer = new AppUser();
        ThingListingResponseDTO listing = ThingListingResponseDTO.builder()
                .id(8L).title("Ladder").description("Two metres").ownerUsername("owner").available(true).build();
        when(sharingService.getListingDetail(8L, viewer)).thenReturn(listing);

        var preview = new ThingPreviewReadService(sharingService).getPreview(8L, viewer);

        assertEquals("Ladder", preview.title());
        assertEquals("Two metres", preview.summary());
        assertEquals(true, preview.available());
    }
}
