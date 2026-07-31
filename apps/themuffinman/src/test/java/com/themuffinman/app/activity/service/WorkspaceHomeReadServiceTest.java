package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.WorkspaceCommandCatalogResponseDTO;
import com.themuffinman.app.activity.dto.WorkspaceCommandItemDTO;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceHomeReadServiceTest {
    @Test
    void exposesOnlyLauncherDestinationsPermittedByTheCommandCatalog() {
        WorkspaceCommandCatalogService catalogService = mock(WorkspaceCommandCatalogService.class);
        AppUser user = new AppUser();
        user.setUsername("Josip");
        when(catalogService.getCatalog(user)).thenReturn(WorkspaceCommandCatalogResponseDTO.builder()
                .navigation(List.of(command("/work/find"), command("/business/find")))
                .create(List.of(command("/things")))
                .build());

        var home = new WorkspaceHomeReadService(catalogService).getHome(user);

        assertEquals("workspace-home-v1", home.getContractVersion());
        assertEquals("Josip", home.getGreetingName());
        assertEquals(List.of("work", "things", "services"), home.getLauncherActions().stream().map(action -> action.getId()).toList());
    }

    private WorkspaceCommandItemDTO command(String route) {
        return WorkspaceCommandItemDTO.builder().route(route).build();
    }
}
