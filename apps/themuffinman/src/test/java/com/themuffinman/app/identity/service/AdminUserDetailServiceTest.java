package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.AdminUserDetailDTO;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.social.service.CircleRelationshipReadService;
import com.themuffinman.app.workmarket.dto.AppUserRoleOptionDTO;
import com.themuffinman.app.workmarket.dto.VisionOptionsDTO;
import com.themuffinman.app.workmarket.service.WorkmarketOptionsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailServiceTest {

    @Mock
    private AppUserLookupService appUserLookupService;

    @Mock
    private AppUserReadService appUserReadService;

    @Mock
    private IdentityUserSummaryAssembler identityUserSummaryAssembler;

    @Mock
    private CircleReadService circleReadService;

    @Mock
    private CircleRelationshipReadService circleRelationshipReadService;

    @Mock
    private WorkmarketOptionsService workmarketOptionsService;

    @InjectMocks
    private AdminUserDetailService adminUserDetailService;

    @Test
    void getDetailRejectsNonAdminViewer() {
        AppUser viewer = new AppUser();
        viewer.setRole(AppUserRole.USER);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminUserDetailService.getDetail(3L, viewer));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getDetailReturnsUserWithAdminOptions() {
        AppUser admin = new AppUser();
        admin.setId(1L);
        admin.setRole(AppUserRole.ADMIN);

        AppUser target = new AppUser();
        target.setId(7L);
        target.setRole(AppUserRole.USER);

        AppUserResponseDTO dto = AppUserResponseDTO.builder()
                .id(7L)
                .username("nina")
                .build();
        VisionOptionsDTO options = VisionOptionsDTO.builder()
                .appUserRoles(List.of(AppUserRoleOptionDTO.builder().value(AppUserRole.USER).label("User").build()))
                .locationModes(List.of())
                .exactLocationVisibilityScopes(List.of())
                .build();
        List<CircleGroupResponseDTO> circles = List.of(CircleGroupResponseDTO.builder().id(5L).name("Friends").build());
        List<CircleContactDTO> contacts = List.of(CircleContactDTO.builder().userId(11L).username("mark").build());

        when(appUserLookupService.requireById(7L)).thenReturn(target);
        when(appUserReadService.countQuestsByCreatorId(7L)).thenReturn(2L);
        when(appUserReadService.getOpenQuestsByCreatorId(7L)).thenReturn(List.of());
        when(identityUserSummaryAssembler.buildProfileSummary(target, 2L, List.of())).thenReturn(dto);
        when(workmarketOptionsService.getOptions(target)).thenReturn(options);
        when(circleReadService.getCirclesForUserAsAdmin(7L, admin)).thenReturn(circles);
        when(circleReadService.getConnectionsForUserAsAdmin(7L, admin)).thenReturn(contacts);

        AdminUserDetailDTO result = adminUserDetailService.getDetail(7L, admin);

        assertEquals(7L, result.getUser().getId());
        assertEquals(1, result.getAppUserRoles().size());
        assertEquals(1, result.getCircles().size());
        assertEquals(1, result.getContacts().size());
    }
}
