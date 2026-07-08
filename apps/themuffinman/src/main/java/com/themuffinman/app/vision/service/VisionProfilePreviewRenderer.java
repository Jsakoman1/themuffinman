package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
class VisionProfilePreviewRenderer {

    private final AppUserReadService appUserReadService;
    private final AppUserMgr appUserMgr;

    VisionCapabilityPreviewDTO previewProfileDraft(AppUser currentUser, String username, String profileDescription) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
        String effectiveUsername = hasText(username) ? username.trim() : existingUser.getUsername();
        String effectiveDescription = profileDescription != null ? profileDescription.trim() : existingUser.getProfileDescription();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "profile_username", "Username", effectiveUsername);
        addItem(items, "profile_description", "Profile description", effectiveDescription);
        addItem(items, "profile_email", "Email", existingUser.getEmail());

        int changedFieldCount = 0;
        if (hasText(username) && !username.trim().equals(existingUser.getUsername())) {
            changedFieldCount++;
        }
        if (profileDescription != null && !profileDescription.trim().equals(nullToEmpty(existingUser.getProfileDescription()))) {
            changedFieldCount++;
        }

        String summary = changedFieldCount == 0
                ? "Your current profile values are loaded. Add a username or profile description change to continue."
                : "Review " + changedFieldCount + " profile change" + (changedFieldCount == 1 ? "" : "s") + " before confirmation.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_profile")
                .title("Profile draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewProfileLocationDraft(AppUser currentUser, String locationMode, String locationLabel) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
        UserLocationSettingsDTO currentSettings = appUserMgr.toDto(existingUser).getLocationSettings();
        String effectiveMode = hasText(locationMode)
                ? locationMode.trim()
                : currentSettings == null || currentSettings.getMode() == null ? null : currentSettings.getMode().name();
        String effectiveLabel = locationLabel != null
                ? locationLabel.trim()
                : currentSettings == null ? null : currentSettings.getLabel();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "profile_location_mode", "Location mode", effectiveMode);
        addItem(items, "profile_location_label", "Location", effectiveLabel);

        String summary = hasText(locationMode) || locationLabel != null
                ? "Review the profile location changes before confirmation."
                : "Your current profile location values are loaded. Add a location mode or location label change to continue.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_profile_location")
                .title("Profile location draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void addItem(List<VisionSlotSummaryDTO> items, String slotId, String label, String value) {
        VisionCapabilityPreviewSupport.addItem(items, slotId, label, value);
    }
}
