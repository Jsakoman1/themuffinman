package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.PersonalWorkspaceShortcut;
import com.themuffinman.app.identity.repository.PersonalWorkspaceShortcutRepository;
import com.themuffinman.app.identity.repository.WorkspaceRailPreferenceRepository;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PersonalShortcutServiceTest {
    @Test void pinUsesVisibilityScopedQuestReadBeforePersisting() {
        var repository = mock(PersonalWorkspaceShortcutRepository.class); var railPreferences = mock(WorkspaceRailPreferenceRepository.class); var quests = mock(WorkmarketQuestReadService.class); var user = new AppUser(); user.setId(4L);
        when(quests.getQuestResponseById(7L, user)).thenReturn(QuestResponseDTO.builder().id(7L).title("Visible quest").build());
        when(repository.findByOwnerIdAndTargetTypeAndTargetId(4L, "QUEST", 7L)).thenReturn(java.util.Optional.empty());
        new PersonalShortcutService(repository, railPreferences, quests).pinQuest(7L, user);
        verify(repository).save(any());
    }

    @Test void omitsDeletedOrNoLongerVisiblePinnedQuestWithoutFailingTheWorkspaceRead() {
        var repository = mock(PersonalWorkspaceShortcutRepository.class); var railPreferences = mock(WorkspaceRailPreferenceRepository.class); var quests = mock(WorkmarketQuestReadService.class); var user = new AppUser(); user.setId(4L);
        var staleShortcut = new PersonalWorkspaceShortcut(); staleShortcut.setTargetType("QUEST"); staleShortcut.setTargetId(7L);
        when(repository.findByOwnerIdOrderByCreatedAtDesc(4L)).thenReturn(java.util.List.of(staleShortcut));
        when(quests.getQuestResponseById(7L, user)).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Quest not found"));

        var shortcuts = new PersonalShortcutService(repository, railPreferences, quests).getMine(user);

        org.junit.jupiter.api.Assertions.assertTrue(shortcuts.isEmpty());
    }

    @Test void keepsRailPreferenceViewerScopedAndRejectsUnsafeWidths() {
        var repository = mock(PersonalWorkspaceShortcutRepository.class); var railPreferences = mock(WorkspaceRailPreferenceRepository.class); var quests = mock(WorkmarketQuestReadService.class); var user = new AppUser(); user.setId(4L);
        when(railPreferences.findByOwnerId(4L)).thenReturn(java.util.Optional.empty());
        var service = new PersonalShortcutService(repository, railPreferences, quests);

        org.junit.jupiter.api.Assertions.assertEquals(240, service.getRailPreference(user).getRailWidthPx());
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.updateRailPreference(281, user));
    }
}
