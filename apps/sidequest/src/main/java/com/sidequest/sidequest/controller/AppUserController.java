package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.AppUserRequestDTO;
import com.sidequest.sidequest.dto.AppUserResponseDTO;
import com.sidequest.sidequest.dto.UserProfileViewDTO;
import com.sidequest.sidequest.mapper.AppUserMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.service.AppUserService;
import com.sidequest.sidequest.service.UserProfileViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/app_users")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final AppUserMgr appUserMgr;
    private final UserProfileViewService userProfileViewService;

    @PostMapping
    public AppUserResponseDTO createAppUser(@Valid @RequestBody AppUserRequestDTO dto) {
        return toDto(appUserService.createAppUser(dto));
    }

    @GetMapping
    public List<AppUserResponseDTO> getAllAppUsers() {
        return appUserService.getAllAppUsers()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/me")
    public AppUserResponseDTO getCurrentAppUser(@AuthenticationPrincipal AppUser currentUser) {
        return toDto(currentUser);
    }

    @GetMapping("/{id}")
    public AppUserResponseDTO getAppUser(@PathVariable long id) {
        AppUser appUser = appUserService.getAppUser(id);
        AppUserResponseDTO dto = toDto(appUser);
        return appUserMgr.withProfileStats(
                dto,
                appUserService.countQuestsByCreatorId(appUser.getId()),
                appUserService.getOpenQuestsByCreatorId(appUser.getId())
        );
    }

    @GetMapping("/{id}/profile-view")
    public UserProfileViewDTO getProfileView(
            @PathVariable long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return userProfileViewService.getProfileView(id, currentUser);
    }

    @DeleteMapping("/{id}")
    public void deleteAppUser(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        appUserService.deleteUser(id, currentUser);
    }

    @PutMapping("/{id}")
    public AppUserResponseDTO updateAppUser(@PathVariable long id, @Valid @RequestBody AppUserRequestDTO dto) {
        return toDto(appUserService.updateAppUserAsAdmin(id, dto));
    }

    @PutMapping("/me")
    public AppUserResponseDTO updateCurrentAppUser(@AuthenticationPrincipal AppUser currentUser, @Valid @RequestBody AppUserRequestDTO dto) {
        return toDto(appUserService.updateAppUser(currentUser.getId(), dto));
    }

    private AppUserResponseDTO toDto(AppUser appUser) {
        return appUserMgr.toDto(appUser);
    }
}
