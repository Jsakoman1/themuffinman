package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.config.BusinessProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessOwnerDashboardReadServiceTest {

    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private BusinessOfferingRepository businessOfferingRepository;
    @Mock private BusinessOwnerScheduleReadService businessOwnerScheduleReadService;
    @Spy private BusinessProperties businessProperties = new BusinessProperties();
    @InjectMocks private BusinessOwnerDashboardReadService service;

    @Test
    void scopesDashboardToRequestedOwnedProfile() {
        AppUser owner = owner();
        BusinessProfile first = profile(10L, owner, "First");
        BusinessProfile second = profile(11L, owner, "Second");
        BusinessOwnerScheduleSummaryDTO summary = BusinessOwnerScheduleSummaryDTO.builder()
                .timezone("Europe/Zurich").todayCount(2).pendingConfirmationCount(1).upcomingCount(3).nextItems(List.of()).build();

        when(businessProfileRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(first, second));
        when(businessOwnerScheduleReadService.getMyScheduleSummary(owner, second.getId())).thenReturn(summary);
        when(businessOfferingRepository.findByBusinessProfileId(second.getId(), owner.getId())).thenReturn(List.of());

        var dashboard = service.getMyDashboard(owner, second.getId());

        assertEquals(second.getId(), dashboard.getBusinessProfileId());
        assertEquals("Second", dashboard.getBusinessName());
        assertEquals(2, dashboard.getTodayCount());
    }

    @Test
    void rejectsProfileOutsideOwnersWorkspace() {
        AppUser owner = owner();
        when(businessProfileRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(profile(10L, owner, "First")));

        assertThrows(RuntimeException.class, () -> service.getMyDashboard(owner, 99L));
    }

    private AppUser owner() { AppUser owner = new AppUser(); owner.setId(1L); owner.setUsername("owner"); return owner; }
    private BusinessProfile profile(Long id, AppUser owner, String name) { BusinessProfile profile = new BusinessProfile(); profile.setId(id); profile.setOwner(owner); profile.setBusinessName(name); profile.setSlug(name.toLowerCase()); profile.setTimezone("Europe/Zurich"); return profile; }
}
