package com.themuffinman.app.search.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.search.dto.SavedSearchIntentRequestDTO;
import com.themuffinman.app.search.model.SavedSearchIntent;
import com.themuffinman.app.search.repository.SavedSearchIntentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedSearchIntentServiceTest {
    @Mock private SavedSearchIntentRepository repository;
    @InjectMocks private SavedSearchIntentService service;

    @Test
    void createBoundsAndNormalizesFamily() {
        when(repository.save(any(SavedSearchIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SavedSearchIntentRequestDTO request = new SavedSearchIntentRequestDTO();
        request.setQuery("  dog groomer  "); request.setEntityFamily("BUSINESS");
        var result = service.create(request, user());
        assertEquals("dog groomer", result.getQuery());
        assertEquals("business", result.getEntityFamily());
    }

    @Test
    void updateRequiresOwnedIntent() {
        when(repository.findOwnedById(7L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(7L, new SavedSearchIntentRequestDTO(), user()));
    }

    private AppUser user() { AppUser user = new AppUser(); user.setId(1L); return user; }
}
