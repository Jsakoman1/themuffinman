package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.AppUserRequestDTO;
import com.sidequest.sidequest.dto.QuestResponseDTO;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.mapper.QuestMgr;
import com.sidequest.sidequest.repository.QuestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestMgr questMgr;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void createAppUserRejectsDuplicateEmailIgnoringCase() {
        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail(" Taken@Example.com ");
        dto.setUsername("new-user");
        dto.setPassword("strong-password");
        when(appUserRepository.existsByEmail("taken@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.createAppUser(dto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void createAppUserRejectsInvalidAccountInput() {
        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail("bad-email");
        dto.setUsername("ab");
        dto.setPassword("short");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.createAppUser(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deleteUserRejectsDeletingCurrentUser() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.deleteUser(1L, currentUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deleteUserRejectsDeletingLastAdmin() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        AppUser lastAdmin = new AppUser();
        lastAdmin.setId(2L);
        lastAdmin.setRole(AppUserRole.ADMIN);
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(lastAdmin));
        when(appUserRepository.countByRole(AppUserRole.ADMIN)).thenReturn(1L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.deleteUser(2L, currentUser));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void updateAppUserThrowsConflictWhenEmailAlreadyExistsOnAnotherUser() {
        AppUser existingUser = new AppUser();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("old");

        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail("taken@example.com");
        dto.setUsername("new-name");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.updateAppUser(1L, dto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void adminUpdateAppUserCanChangeRoleAndPassword() {
        AppUser existingUser = new AppUser();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("old");
        existingUser.setRole(AppUserRole.USER);

        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail("new@example.com");
        dto.setUsername("new-name");
        dto.setRole(AppUserRole.ADMIN);
        dto.setPassword("new-password");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.save(existingUser)).thenReturn(existingUser);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded");

        AppUser updated = appUserService.updateAppUserAsAdmin(1L, dto);

        assertEquals(AppUserRole.ADMIN, updated.getRole());
        assertEquals("encoded", updated.getPasswordHash());
    }

    @Test
    void updateAppUserCanChangeProfileDetails() {
        AppUser existingUser = new AppUser();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("old");
        existingUser.setProfileDescription("old bio");
        existingUser.setProfileAvatarDataUrl("data:image/jpeg;base64,old");

        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail("new@example.com");
        dto.setUsername("new-name");
        dto.setProfileDescription("new bio");
        dto.setProfileAvatarDataUrl("data:image/jpeg;base64,new");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.save(existingUser)).thenReturn(existingUser);

        AppUser updated = appUserService.updateAppUser(1L, dto);

        assertEquals("new bio", updated.getProfileDescription());
        assertEquals("data:image/jpeg;base64,new", updated.getProfileAvatarDataUrl());
    }

    @Test
    void updateAppUserSanitizesHtmlProfileDescription() {
        AppUser existingUser = new AppUser();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("old");

        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail("new@example.com");
        dto.setUsername("new-name");
        dto.setProfileDescription("<p>Hello</p><script>alert(1)</script>");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.save(existingUser)).thenReturn(existingUser);

        AppUser updated = appUserService.updateAppUser(1L, dto);

        assertEquals("<p>Hello</p>", updated.getProfileDescription());
    }

    @Test
    void adminUpdateAppUserPreservesProfileDetailsWhenNotProvided() {
        AppUser existingUser = new AppUser();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("old");
        existingUser.setProfileDescription("kept bio");
        existingUser.setProfileAvatarDataUrl("data:image/jpeg;base64,kept");

        AppUserRequestDTO dto = new AppUserRequestDTO();
        dto.setEmail("new@example.com");
        dto.setUsername("new-name");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(appUserRepository.save(existingUser)).thenReturn(existingUser);

        AppUser updated = appUserService.updateAppUserAsAdmin(1L, dto);

        assertEquals("kept bio", updated.getProfileDescription());
        assertEquals("data:image/jpeg;base64,kept", updated.getProfileAvatarDataUrl());
        assertNull(updated.getPasswordHash());
    }

    @Test
    void countProfileStatsUsesRepositories() {
        when(questRepository.countByCreatorIdAndStatus(7L, QuestStatus.OPEN)).thenReturn(3L);

        assertEquals(3L, appUserService.countQuestsByCreatorId(7L));
    }

    @Test
    void getOpenQuestsByCreatorIdReturnsMappedOpenQuests() {
        Quest quest = new Quest();
        quest.setId(11L);

        QuestResponseDTO questResponseDTO = QuestResponseDTO.builder().id(11L).build();

        when(questRepository.findByCreatorIdAndStatusOrderByIdDesc(7L, QuestStatus.OPEN)).thenReturn(List.of(quest));
        when(questMgr.toDto(quest)).thenReturn(questResponseDTO);

        List<QuestResponseDTO> openQuests = appUserService.getOpenQuestsByCreatorId(7L);
        assertEquals(1, openQuests.size());
        assertEquals(11L, openQuests.getFirst().getId());
    }
}
