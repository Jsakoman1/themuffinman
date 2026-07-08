package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.dto.AppUserRequestDTO;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.dto.UserLocationSettingsRequestDTO;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class VisionProfileMutationAdapter {

    private final AppUserService appUserService;
    private final AppUserReadService appUserReadService;
    private final AppUserMgr appUserMgr;

    AppUserResponseDTO updateProfile(AppUser currentUser, String username, String profileDescription) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
        AppUserRequestDTO request = AppUserRequestDTO.builder()
                .email(existingUser.getEmail())
                .username(hasText(username) ? username.trim() : existingUser.getUsername())
                .profileDescription(profileDescription == null ? existingUser.getProfileDescription() : profileDescription.trim())
                .profileAvatarDataUrl(existingUser.getProfileAvatarDataUrl())
                .locationSettings(toLocationRequest(appUserMgr.toDto(existingUser).getLocationSettings()))
                .build();
        AppUser updatedUser = appUserService.updateAppUser(existingUser.getId(), request);
        return appUserMgr.withProfileStats(
                appUserMgr.toDto(updatedUser),
                appUserReadService.countQuestsByCreatorId(updatedUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(updatedUser.getId())
        );
    }

    AppUserResponseDTO updateProfileLocation(AppUser currentUser, String locationMode, String locationLabel) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
        UserLocationSettingsDTO currentSettings = appUserMgr.toDto(existingUser).getLocationSettings();
        UserLocationMode mode = locationMode == null || locationMode.isBlank()
                ? currentSettings == null || currentSettings.getMode() == null ? UserLocationMode.OFF : currentSettings.getMode()
                : UserLocationMode.valueOf(TextValueNormalizer.upperTrimToEmpty(locationMode));
        AppUserRequestDTO request = AppUserRequestDTO.builder()
                .email(existingUser.getEmail())
                .username(existingUser.getUsername())
                .profileDescription(existingUser.getProfileDescription())
                .profileAvatarDataUrl(existingUser.getProfileAvatarDataUrl())
                .locationSettings(toLocationRequestForProfileLocation(currentSettings, mode, locationLabel))
                .build();
        AppUser updatedUser = appUserService.updateAppUser(existingUser.getId(), request);
        return appUserMgr.withProfileStats(
                appUserMgr.toDto(updatedUser),
                appUserReadService.countQuestsByCreatorId(updatedUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(updatedUser.getId())
        );
    }

    private UserLocationSettingsRequestDTO toLocationRequest(UserLocationSettingsDTO source) {
        if (source == null) {
            return null;
        }

        return UserLocationSettingsRequestDTO.builder()
                .mode(source.getMode())
                .defaultRadiusKm(source.getDefaultRadiusKm())
                .exactVisibilityScope(source.getExactVisibilityScope())
                .exactVisibleCircleIds(source.getExactVisibleCircleIds())
                .exactVisibleUserIds(source.getExactVisibleUserIds())
                .provider(source.getProvider())
                .providerPlaceId(source.getProviderPlaceId())
                .label(source.getLabel())
                .countryCode(source.getCountryCode())
                .country(source.getCountry())
                .locality(source.getLocality())
                .postalCode(source.getPostalCode())
                .street(source.getStreet())
                .houseNumber(source.getHouseNumber())
                .latitude(source.getLatitude())
                .longitude(source.getLongitude())
                .resolvedAt(source.getResolvedAt())
                .build();
    }

    private UserLocationSettingsRequestDTO toLocationRequestForProfileLocation(
            UserLocationSettingsDTO source,
            UserLocationMode mode,
            String locationLabel
    ) {
        UserLocationSettingsRequestDTO.UserLocationSettingsRequestDTOBuilder builder = UserLocationSettingsRequestDTO.builder()
                .mode(mode)
                .defaultRadiusKm(source == null ? null : source.getDefaultRadiusKm())
                .exactVisibilityScope(source == null ? null : source.getExactVisibilityScope())
                .exactVisibleCircleIds(source == null ? null : source.getExactVisibleCircleIds())
                .exactVisibleUserIds(source == null ? null : source.getExactVisibleUserIds());
        if (mode == UserLocationMode.OFF) {
            return builder.build();
        }

        String nextLabel = locationLabel != null ? locationLabel.trim() : source == null ? null : source.getLabel();
        boolean keepResolvedData = source != null
                && nextLabel != null
                && nextLabel.equals(nullToEmpty(source.getLabel()).trim());
        if (keepResolvedData) {
            return builder
                    .provider(source.getProvider())
                    .providerPlaceId(source.getProviderPlaceId())
                    .label(source.getLabel())
                    .countryCode(source.getCountryCode())
                    .country(source.getCountry())
                    .locality(source.getLocality())
                    .postalCode(source.getPostalCode())
                    .street(source.getStreet())
                    .houseNumber(source.getHouseNumber())
                    .latitude(source.getLatitude())
                    .longitude(source.getLongitude())
                    .resolvedAt(source.getResolvedAt())
                    .build();
        }

        return builder
                .label(nextLabel)
                .provider(null)
                .providerPlaceId(null)
                .countryCode(null)
                .country(null)
                .locality(null)
                .postalCode(null)
                .street(null)
                .houseNumber(null)
                .latitude(null)
                .longitude(null)
                .resolvedAt(null)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
