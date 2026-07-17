package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionBusinessBookingMutationExecutionAdapterTest {
    @Test
    void confirmsBookingThroughBackendService() {
        VisionBusinessBookingMutationService service = mock(VisionBusinessBookingMutationService.class);
        VisionConversation conversation = conversation();
        when(service.confirm(42L, conversation.getOwner())).thenReturn(BusinessBookingResponseDTO.builder().build());
        assertTrue(new VisionConfirmBookingExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).confirm(42L, conversation.getOwner());
    }

    @Test
    void cancelsBookingThroughBackendService() {
        VisionBusinessBookingMutationService service = mock(VisionBusinessBookingMutationService.class);
        VisionConversation conversation = conversation();
        when(service.cancel(42L, conversation.getOwner())).thenReturn(BusinessBookingResponseDTO.builder().build());
        assertTrue(new VisionCancelBookingExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).cancel(42L, conversation.getOwner());
    }

    @Test
    void rejectsBookingThroughBackendService() {
        VisionBusinessBookingMutationService service = mock(VisionBusinessBookingMutationService.class);
        VisionConversation conversation = conversation();
        when(service.reject(42L, conversation.getOwner())).thenReturn(BusinessBookingResponseDTO.builder().build());
        assertTrue(new VisionRejectBookingExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).reject(42L, conversation.getOwner());
    }

    @Test
    void completesBookingThroughBackendService() {
        VisionBusinessBookingMutationService service = mock(VisionBusinessBookingMutationService.class);
        VisionConversation conversation = conversation();
        when(service.complete(42L, conversation.getOwner())).thenReturn(BusinessBookingResponseDTO.builder().build());
        assertTrue(new VisionCompleteBookingExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).complete(42L, conversation.getOwner());
    }

    @Test
    void marksNoShowThroughBackendService() {
        VisionBusinessBookingMutationService service = mock(VisionBusinessBookingMutationService.class);
        VisionConversation conversation = conversation();
        when(service.noShow(42L, conversation.getOwner())).thenReturn(BusinessBookingResponseDTO.builder().build());
        assertTrue(new VisionMarkBookingNoShowExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).noShow(42L, conversation.getOwner());
    }

    private VisionConversation conversation() {
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setSlotData(new LinkedHashMap<>(Map.of("booking_id", "42")));
        return conversation;
    }
}
