package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.ProfileGalleryImageRequestDTO;
import com.themuffinman.app.identity.mapper.ProfileGalleryImageMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.ProfileGalleryImage;
import com.themuffinman.app.identity.repository.ProfileGalleryImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileGalleryServiceTest {
    @Mock
    private ProfileGalleryImageRepository repository;

    @Spy
    private ProfileGalleryImageMgr mapper = new ProfileGalleryImageMgr();

    @InjectMocks
    private ProfileGalleryService service;

    @Test
    void createTrimsValuesAndPreservesOrdering() {
        AppUser owner = user(1L, "owner");
        when(repository.save(any(ProfileGalleryImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ProfileGalleryImageRequestDTO request = new ProfileGalleryImageRequestDTO();
        request.setImageUrl("  https://cdn.example.com/photo.jpg  ");
        request.setAltText("  Profile photo  ");
        request.setSortOrder(3);

        var result = service.create(owner, request);

        assertEquals("https://cdn.example.com/photo.jpg", result.getImageUrl());
        assertEquals("Profile photo", result.getAltText());
        assertEquals(3, result.getSortOrder());
    }

    @Test
    void createRejectsBlankUrl() {
        ProfileGalleryImageRequestDTO request = new ProfileGalleryImageRequestDTO();
        request.setImageUrl("   ");
        assertThrows(ResponseStatusException.class, () -> service.create(user(1L, "owner"), request));
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }
}
