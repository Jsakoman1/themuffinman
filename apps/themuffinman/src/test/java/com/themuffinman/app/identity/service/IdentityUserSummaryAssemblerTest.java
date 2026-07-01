package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationSettingsViewService;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

class IdentityUserSummaryAssemblerTest {

    @Test
    void buildProfileSummaryDelegatesToAppUserMapperAndAddsStats() {
        LocationSettingsViewService locationSettingsViewService = Mockito.mock(LocationSettingsViewService.class);
        AppUserMgr appUserMgr = Mockito.spy(new AppUserMgr(locationSettingsViewService));
        IdentityUserSummaryAssembler assembler = new IdentityUserSummaryAssembler(appUserMgr);

        AppUser profileUser = new AppUser();
        profileUser.setId(7L);
        profileUser.setUsername("owner");
        profileUser.setEmail("owner@example.com");

        AppUserResponseDTO base = AppUserResponseDTO.builder().id(7L).username("owner").build();
        doReturn(base).when(appUserMgr).toDto(profileUser);

        QuestResponseDTO openQuest = QuestResponseDTO.builder().id(1L).title("Task").build();
        AppUserResponseDTO result = assembler.buildProfileSummary(profileUser, 2L, List.of(openQuest));

        assertEquals(2L, result.getOpenQuestCount());
        assertEquals(1, result.getOpenQuests().size());
    }
}
