package com.themuffinman.app.rides.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.CommutePreferenceRequestDTO;
import com.themuffinman.app.rides.model.CommutePreference;
import com.themuffinman.app.rides.repository.CommutePreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommutePreferenceServiceTest {
    @Mock private CommutePreferenceRepository repository;
    @InjectMocks private CommutePreferenceService service;

    @Test
    void enabledMatchingRequiresExplicitConsent() {
        CommutePreferenceRequestDTO request = new CommutePreferenceRequestDTO();
        request.setEnabled(true);
        request.setConsentGranted(false);
        assertThrows(RuntimeException.class, () -> service.updateMine(request, user()));
    }

    @Test
    void updateNormalizesWeekdaysAndReturnsPreference() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(CommutePreference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CommutePreferenceRequestDTO request = new CommutePreferenceRequestDTO();
        request.setEnabled(true);
        request.setConsentGranted(true);
        request.setHomeArea("North area");
        request.setWorkArea("Central area");
        request.setWeekdays(List.of(5, 1, 5));
        request.setDepartureTime(LocalTime.of(7, 30));
        var result = service.updateMine(request, user());
        assertEquals(List.of(1, 5), result.getWeekdays());
        assertEquals("North area", result.getHomeArea());
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(1L);
        return user;
    }
}
