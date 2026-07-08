package com.themuffinman.app.identity.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.identity.dto.AppUserRequestDTO;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.dto.AdminUserDetailDTO;
import com.themuffinman.app.identity.dto.UserProfileViewDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AdminUserDetailService;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.workmarket.dto.VisionOptionsDTO;
import com.themuffinman.app.workmarket.service.WorkmarketOptionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app_users")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final AppUserReadService appUserReadService;
    private final AppUserMgr appUserMgr;
    private final AdminUserDetailService adminUserDetailService;
    private final UserProfileViewService userProfileViewService;
    private final WorkmarketOptionsService workmarketOptionsService;

    @PostMapping
    public ActionResultDTO createAppUser(@Valid @RequestBody AppUserRequestDTO dto) {
        appUserService.createAppUser(dto);
        return ActionResults.of("CREATE_APP_USER", "User created.");
    }

    @GetMapping
    public List<AppUserResponseDTO> getAllAppUsers(@RequestParam(value = "q", required = false) String query) {
        return appUserReadService.getAllAppUsers(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/options")
    public VisionOptionsDTO getAppUserOptions(@AuthenticationPrincipal AppUser currentUser) {
        return workmarketOptionsService.getOptions(currentUser);
    }

    @GetMapping("/me")
    public AppUserResponseDTO getCurrentAppUser(@AuthenticationPrincipal AppUser currentUser) {
        return toDto(currentUser);
    }

    @GetMapping("/{id}")
    public AppUserResponseDTO getAppUser(@PathVariable long id) {
        AppUser appUser = appUserReadService.getAppUser(id);
        AppUserResponseDTO dto = toDto(appUser);
        return appUserMgr.withProfileStats(
                dto,
                appUserReadService.countQuestsByCreatorId(appUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(appUser.getId())
        );
    }

    @GetMapping("/{id}/admin-detail")
    public AdminUserDetailDTO getAdminUserDetail(
            @PathVariable long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return adminUserDetailService.getDetail(id, currentUser);
    }

    @GetMapping("/{id}/profile-view")
    public UserProfileViewDTO getProfileView(
            @PathVariable long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return userProfileViewService.getProfileView(id, currentUser);
    }

    @DeleteMapping("/{id}")
    public ActionResultDTO deleteAppUser(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        appUserService.deleteUser(id, currentUser);
        return ActionResults.of("DELETE_APP_USER", "User deleted.");
    }

    @PutMapping("/{id}")
    public ActionResultDTO updateAppUser(@PathVariable long id, @Valid @RequestBody AppUserRequestDTO dto) {
        appUserService.updateAppUserAsAdmin(id, dto);
        return ActionResults.of("UPDATE_APP_USER", "User updated.");
    }

    @PutMapping("/me")
    public AppUserResponseDTO updateCurrentAppUser(@AuthenticationPrincipal AppUser currentUser, @Valid @RequestBody AppUserRequestDTO dto) {
        return toDto(appUserService.updateAppUser(currentUser.getId(), dto));
    }

    private AppUserResponseDTO toDto(AppUser appUser) {
        return appUserMgr.toDto(appUser);
    }
}
