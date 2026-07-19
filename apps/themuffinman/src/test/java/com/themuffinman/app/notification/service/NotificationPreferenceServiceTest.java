package com.themuffinman.app.notification.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.dto.NotificationPreferenceUpdateDTO;
import com.themuffinman.app.notification.model.NotificationPreferenceCategory;
import com.themuffinman.app.notification.model.NotificationPreferenceLevel;
import com.themuffinman.app.notification.model.NotificationPreference;
import com.themuffinman.app.notification.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {
    @Mock
    private NotificationPreferenceRepository repository;

    @InjectMocks
    private NotificationPreferenceService service;

    @Test
    void returnsCompleteDefaultMatrixWithSystemRequired() {
        AppUser user = user(7L);
        when(repository.findByUserId(7L)).thenReturn(List.of());

        var result = service.getForUser(user);

        assertEquals(18, result.getItems().size());
        assertTrue(result.getItems().stream().filter(item -> item.getCategory() == NotificationPreferenceCategory.SYSTEM).allMatch(item -> item.isRequired() && item.isEnabled()));
    }

    @Test
    void rejectsDisablingSystemNotifications() {
        AppUser user = user(7L);
        NotificationPreferenceUpdateDTO update = new NotificationPreferenceUpdateDTO();
        update.setCategory(NotificationPreferenceCategory.SYSTEM);
        update.setLevel(NotificationPreferenceLevel.EMAIL);
        update.setEnabled(false);

        assertThrows(ResponseStatusException.class, () -> service.update(user, List.of(update)));
    }

    @Test
    void defaultsInAppDeliveryToEnabledAndHonorsSavedCategoryPreference() {
        AppUser user = user(7L);
        NotificationPreference disabled = new NotificationPreference();
        disabled.setCategory(NotificationPreferenceCategory.WORK);
        disabled.setLevel(NotificationPreferenceLevel.IN_APP);
        disabled.setEnabled(false);
        when(repository.findByUserId(7L)).thenReturn(List.of(disabled));

        assertTrue(service.isInAppDeliveryEnabled(user, NotificationPreferenceCategory.CIRCLE));
        assertFalse(service.isInAppDeliveryEnabled(user, NotificationPreferenceCategory.WORK));
        assertTrue(service.isInAppDeliveryEnabled(user, NotificationPreferenceCategory.SYSTEM));
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("tester");
        return user;
    }
}
