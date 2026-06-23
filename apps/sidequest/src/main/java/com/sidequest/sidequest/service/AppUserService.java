package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.AppUserRequestDTO;
import com.sidequest.sidequest.mapper.QuestMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.repository.QuestRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final QuestRepository questRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestMgr questMgr;
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

    public List<AppUser> getAllAppUsers() {
        return appUserRepository.findAll();
    }

    public AppUser getAppUser(Long id) {
        return requireAppUser(id);
    }

    public long countQuestsByCreatorId(Long creatorId) {
        return questRepository.countByCreatorIdAndStatus(creatorId, QuestStatus.OPEN);
    }

    public List<com.sidequest.sidequest.dto.QuestResponseDTO> getOpenQuestsByCreatorId(Long creatorId) {
        return questRepository.findByCreatorIdAndStatusOrderByIdDesc(creatorId, QuestStatus.OPEN)
                .stream()
                .limit(6)
                .map(questMgr::toDto)
                .toList();
    }

    public void deleteUser(Long id, AppUser currentUser) {
        AppUser targetUser = requireAppUser(id);
        if (currentUser != null && id.equals(currentUser.getId())) {
            throw ServiceErrors.badRequest("You cannot delete your own account");
        }
        if (targetUser.getRole() == AppUserRole.ADMIN && appUserRepository.countByRole(AppUserRole.ADMIN) <= 1) {
            throw ServiceErrors.conflict("The last administrator cannot be deleted");
        }
        appUserRepository.delete(targetUser);
    }

    public AppUser updateAppUser(Long id, AppUserRequestDTO dto) {
        validateAccountInput(dto, false);
        AppUser appUser = requireAppUser(id);
        validateUniqueEmail(id, dto.getEmail());
        updateBasicProfile(appUser, dto);
        applyProfileDetails(appUser, dto, true);
        return appUserRepository.save(appUser);
    }

    public AppUser updateAppUserAsAdmin(Long id, AppUserRequestDTO dto) {
        validateAccountInput(dto, false);
        AppUser appUser = requireAppUser(id);
        validateUniqueEmail(id, dto.getEmail());
        updateBasicProfile(appUser, dto);
        applyProfileDetails(appUser, dto, false);
        applyAdminOverrides(appUser, dto);
        return appUserRepository.save(appUser);
    }

    private AppUser requireAppUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> ServiceErrors.notFound(String.format("AppUser not found with id %s", id)));
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
