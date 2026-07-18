package com.themuffinman.app.activity.service;

import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttentionCenterServiceTest {
    @Test void reusesViewerScopedActivityProjection() {
        ActivityReadService activity = mock(ActivityReadService.class); AppUser user = new AppUser(); List<com.themuffinman.app.activity.dto.ActivityItemDTO> items = List.of();
        when(activity.getMine(user)).thenReturn(items);
        assertSame(items, new AttentionCenterService(activity).getMine(user));
    }
}
