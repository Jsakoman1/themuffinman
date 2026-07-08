package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingPolicyRequestDTO;
import com.themuffinman.app.business.mapper.BusinessBookingPolicyMgr;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingPolicyRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.config.BusinessProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessBookingPolicyServiceTest {

    @Mock
    private BusinessBookingPolicyRepository businessBookingPolicyRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Spy
    private BusinessBookingPolicyMgr businessBookingPolicyMgr = new BusinessBookingPolicyMgr();

    private final BusinessProperties businessProperties = new BusinessProperties();

    @InjectMocks
    private BusinessBookingPolicyService businessBookingPolicyService;

    @Test
    void getMyPolicyCreatesDefaultPolicyWhenMissing() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(5L, owner);

        when(businessBookingPolicyRepository.findByOwnerId(owner.getId())).thenReturn(Optional.empty());
        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));
        when(businessBookingPolicyRepository.save(any(BusinessBookingPolicy.class))).thenAnswer(invocation -> {
            BusinessBookingPolicy policy = invocation.getArgument(0);
            policy.setId(8L);
            return policy;
        });

        BusinessBookingPolicyService service = new BusinessBookingPolicyService(
                businessBookingPolicyRepository,
                businessProfileRepository,
                businessBookingPolicyMgr,
                businessProperties
        );

        var result = service.getMyPolicy(owner);

        assertEquals(8L, result.getId());
        assertEquals(businessProperties.getBooking().getDefaultLeadTimeMinutes(), result.getLeadTimeMinutes());
        assertEquals(businessProperties.getBooking().getDefaultMaxAdvanceDays(), result.getMaxAdvanceDays());
    }

    @Test
    void resolveEffectivePolicyDoesNotPersistWhenMissing() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(5L, owner);

        when(businessBookingPolicyRepository.findByOwnerId(owner.getId())).thenReturn(Optional.empty());
        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));

        BusinessBookingPolicyService service = new BusinessBookingPolicyService(
                businessBookingPolicyRepository,
                businessProfileRepository,
                businessBookingPolicyMgr,
                businessProperties
        );

        BusinessBookingPolicy result = service.resolveEffectivePolicy(owner);

        assertEquals(businessProperties.getBooking().getDefaultLeadTimeMinutes(), result.getLeadTimeMinutes());
        assertEquals(profile.getId(), result.getBusinessProfile().getId());
        verify(businessBookingPolicyRepository, never()).save(any(BusinessBookingPolicy.class));
    }

    @Test
    void saveMyPolicyUpdatesFields() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(5L, owner);
        BusinessBookingPolicy policy = new BusinessBookingPolicy();
        policy.setId(8L);
        policy.setBusinessProfile(profile);
        policy.setLeadTimeMinutes(10);
        policy.setMaxAdvanceDays(10);
        policy.setCustomerCancellationWindowMinutes(60);
        policy.setOwnerRescheduleWindowMinutes(60);

        when(businessBookingPolicyRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(policy));
        when(businessBookingPolicyRepository.save(any(BusinessBookingPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessBookingPolicyService service = new BusinessBookingPolicyService(
                businessBookingPolicyRepository,
                businessProfileRepository,
                businessBookingPolicyMgr,
                businessProperties
        );

        var result = service.saveMyPolicy(BusinessBookingPolicyRequestDTO.builder()
                .leadTimeMinutes(120)
                .maxAdvanceDays(30)
                .customerCancellationWindowMinutes(1440)
                .ownerRescheduleWindowMinutes(720)
                .requiresOwnerConfirmationDefault(false)
                .allowCustomerCancellation(true)
                .allowOwnerManualApproval(true)
                .allowOwnerManualRejection(false)
                .allowWaitlist(true)
                .build(), owner);

        assertEquals(120, result.getLeadTimeMinutes());
        assertEquals(30, result.getMaxAdvanceDays());
        assertEquals(false, result.isRequiresOwnerConfirmationDefault());
        assertEquals(true, result.isAllowWaitlist());
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }

    private BusinessProfile profile(Long id, AppUser owner) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(id);
        profile.setOwner(owner);
        profile.setBusinessName("Blue Bakery");
        profile.setSlug("blue-bakery");
        return profile;
    }
}
