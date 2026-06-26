package com.themuffinman.app.identity.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.location.dto.UserLocationSettingsRequestDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRequestDTO {
    private @NotBlank @Email @Size(max = 320) String email;
    private @NotBlank @Size(min = 3, max = 50) String username;
    @ContractOptional
    private @Size(min = 8, max = 100) String password;
    @ContractOptional
    @Nullable
    @Size(max = 2000)
    private String profileDescription;
    @ContractOptional
    @Nullable
    @Size(max = 250000)
    @Pattern(regexp = "^data:image/.*", message = "Profile avatar must be an image data URL")
    private String profileAvatarDataUrl;
    @ContractOptional
    @Nullable
    private UserLocationSettingsRequestDTO locationSettings;
    @ContractOptional
    private AppUserRole role;
}
