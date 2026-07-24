package com.themuffinman.app.business.service;

import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.mapper.BusinessProfileMgr;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessWorkspaceContextServiceTest {
    @Mock BusinessProfileRepository profileRepository;
    @Mock BusinessProfileMgr profileMgr;
    @Mock BusinessBookingRepository bookingRepository;
    @Mock BusinessBookingMgr bookingMgr;
    @Mock BusinessBookingPresentationService presentationService;
    @InjectMocks BusinessWorkspaceContextService service;

    @Test
    void returnsEmptySafeContextWhenViewerHasNoOwnedBusiness() {
        AppUser viewer = new AppUser();
        viewer.setId(42L);
        when(profileRepository.findAllByOwnerId(42L)).thenReturn(List.of());
        var context = service.getContext(viewer, null, null, null);
        assertTrue(context.getBusinesses().isEmpty());
        assertTrue(context.getSchedule().isEmpty());
    }

    @Test
    void treatsMissingProfileAsExplicitAllBusinessesAggregate() {
        AppUser viewer = new AppUser();
        viewer.setId(42L);
        BusinessProfile first = new BusinessProfile();
        first.setId(10L);
        BusinessProfile second = new BusinessProfile();
        second.setId(11L);
        when(profileRepository.findAllByOwnerId(42L)).thenReturn(List.of(first, second));
        when(bookingRepository.findDetailedByOwnerIdAndOverlap(org.mockito.ArgumentMatchers.eq(42L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(bookingRepository.findDetailedByCustomerIdAndOverlap(org.mockito.ArgumentMatchers.eq(42L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        var context = service.getContext(viewer, null, null, null);

        assertTrue(context.getBusinesses().isEmpty() || context.getBusinesses().size() == 2);
        org.junit.jupiter.api.Assertions.assertNull(context.getActiveBusinessProfileId());
        verify(bookingRepository).findDetailedByOwnerIdAndOverlap(org.mockito.ArgumentMatchers.eq(42L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(bookingRepository).findDetailedByCustomerIdAndOverlap(org.mockito.ArgumentMatchers.eq(42L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
