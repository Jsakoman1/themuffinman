package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.AppUserRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.model.ProfileFieldVisibility;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.common.normalization.ProfileValueNormalizer;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.UserInputNormalizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final AppUserLookupService appUserLookupService;
    private final PasswordEncoder passwordEncoder;
    private final LocationSettingsService locationSettingsService;
    private final CircleGroupRepository circleGroupRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public AppUser createAppUser(AppUserRequestDTO dto) {
        validateAccountInput(dto, true);
        String email = UserInputNormalizer.normalizeEmail(dto.getEmail());
        validateUniqueEmail(null, email);
        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setUsername(normalizeUsername(dto.getUsername()));
        applyProfileDetails(appUser, dto, true);
        appUser.setRole(dto.getRole() == null ? AppUserRole.USER : dto.getRole());
        appUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        return appUserRepository.save(appUser);
    }

    public void deleteUser(Long id, AppUser currentUser) {
        AppUser targetUser = appUserLookupService.requireById(id);
        if (currentUser != null && id.equals(currentUser.getId())) {
            throw ServiceErrors.badRequest("You cannot delete your own account");
        }
        if (targetUser.getRole() == AppUserRole.ADMIN && appUserRepository.countByRole(AppUserRole.ADMIN) <= 1) {
            throw ServiceErrors.conflict("The last administrator cannot be deleted");
        }
        appUserRepository.delete(targetUser);
    }

    public AppUser updateAppUser(Long id, AppUserRequestDTO dto) {
        // Guided profile drafts still enter the canonical identity validation path.
        validateAccountInput(dto, false);
        AppUser appUser = appUserLookupService.requireById(id);
        validateUniqueEmail(id, dto.getEmail());
        updateBasicProfile(appUser, dto);
        applyProfileDetails(appUser, dto, true);
        return appUserRepository.save(appUser);
    }

    public AppUser updateAppUserAsAdmin(Long id, AppUserRequestDTO dto) {
        validateAccountInput(dto, false);
        AppUser appUser = appUserLookupService.requireById(id);
        validateUniqueEmail(id, dto.getEmail());
        updateBasicProfile(appUser, dto);
        applyProfileDetails(appUser, dto, false);
        applyAdminOverrides(appUser, dto);
        return appUserRepository.save(appUser);
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw ServiceErrors.badRequest("Password is required");
        }

        String normalizedPassword = password.trim();
        if (normalizedPassword.length() < 8 || normalizedPassword.length() > 100) {
            throw ServiceErrors.badRequest("Password must be between 8 and 100 characters");
        }
    }

    private void validateAccountInput(AppUserRequestDTO dto, boolean requirePassword) {
        if (dto == null) {
            throw ServiceErrors.badRequest("App user request is required");
        }

        validateEmail(dto.getEmail());
        validateUsername(dto.getUsername());

        if (requirePassword && dto.getPassword() == null) {
            throw ServiceErrors.badRequest("Password is required");
        }

        if (requirePassword && dto.getPassword() != null) {
            validatePassword(dto.getPassword());
        }
    }

    private void validateUniqueEmail(Long id, String email) {
        String normalizedEmail = UserInputNormalizer.normalizeEmail(email);
        boolean exists = id == null
                ? appUserRepository.existsByEmail(normalizedEmail)
                : appUserRepository.existsByEmailAndIdNot(normalizedEmail, id);
        if (exists) {
            throw ServiceErrors.conflict("Email already exists");
        }
    }

    private void updateBasicProfile(AppUser appUser, AppUserRequestDTO dto) {
        appUser.setUsername(normalizeUsername(dto.getUsername()));
        appUser.setEmail(UserInputNormalizer.normalizeEmail(dto.getEmail()));
    }

    private void applyAdminOverrides(AppUser appUser, AppUserRequestDTO dto) {
        if (dto.getRole() != null) {
            appUser.setRole(dto.getRole());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            appUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
    }

    private void applyProfileDetails(AppUser appUser, AppUserRequestDTO dto, boolean overwriteExisting) {
        if (overwriteExisting || dto.getProfileDescription() != null) {
            appUser.setProfileDescription(RichTextInputValidator.sanitize(dto.getProfileDescription()));
        }

        if (overwriteExisting || dto.getProfileAvatarDataUrl() != null) {
            appUser.setProfileAvatarDataUrl(ProfileValueNormalizer.normalizeAvatarDataUrl(dto.getProfileAvatarDataUrl()));
        }

        if (dto.getProfileDescriptionVisibility() != null) {
            appUser.setProfileDescriptionVisibility(dto.getProfileDescriptionVisibility());
        }
        if (dto.getProfileAvatarVisibility() != null) {
            appUser.setProfileAvatarVisibility(dto.getProfileAvatarVisibility());
        }
        applyProfileVisibilityCircles(appUser, dto.getProfileDescriptionVisibility(), dto.getProfileDescriptionVisibleCircleIds(), true);
        applyProfileVisibilityCircles(appUser, dto.getProfileAvatarVisibility(), dto.getProfileAvatarVisibleCircleIds(), false);

        if (overwriteExisting || dto.getLocationSettings() != null) {
            locationSettingsService.applyUserLocationSettings(appUser, dto.getLocationSettings());
        }
    }

    private void applyProfileVisibilityCircles(AppUser appUser, ProfileFieldVisibility visibility, Set<Long> ids, boolean description) {
        if (visibility != ProfileFieldVisibility.CIRCLES && ids == null) {
            if (description) appUser.getProfileDescriptionVisibleToCircles().clear();
            else appUser.getProfileAvatarVisibleToCircles().clear();
            return;
        }
        if (visibility == ProfileFieldVisibility.CIRCLES && (ids == null || ids.isEmpty())) {
            throw ServiceErrors.badRequest("Select at least one circle for circle-scoped profile visibility");
        }
        Set<Long> selectedIds = ids == null ? Set.of() : ids;
        Set<CircleGroup> ownedCircles = selectedIds.isEmpty()
                ? Set.of()
                : new LinkedHashSet<>(circleGroupRepository.findAllByOwnerIdAndIdIn(appUser.getId(), selectedIds));
        if (ownedCircles.size() != selectedIds.size()) {
            throw ServiceErrors.badRequest("Profile visibility circles must belong to the profile owner");
        }
        if (description) {
            appUser.setProfileDescriptionVisibleToCircles(ownedCircles);
        } else {
            appUser.setProfileAvatarVisibleToCircles(ownedCircles);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private void validateEmail(String email) {
        String normalizedEmail = UserInputNormalizer.normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw ServiceErrors.badRequest("Email is required");
        }

        if (normalizedEmail.length() > 320 || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw ServiceErrors.badRequest("Email is invalid");
        }
    }

    private void validateUsername(String username) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null || normalizedUsername.isBlank()) {
            throw ServiceErrors.badRequest("Username is required");
        }

        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 50) {
            throw ServiceErrors.badRequest("Username must be between 3 and 50 characters");
        }
    }
}
