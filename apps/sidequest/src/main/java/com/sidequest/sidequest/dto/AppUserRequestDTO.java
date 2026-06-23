package com.sidequest.sidequest.dto;

import com.sidequest.sidequest.model.AppUserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRequestDTO {
    private @NotBlank @Email @Size(max = 320) String email;
    private @NotBlank @Size(min = 3, max = 50) String username;
    private @Size(min = 8, max = 100) String password;
    @Size(max = 2000)
    private String profileDescription;
    @Size(max = 250000)
    @Pattern(regexp = "^data:image/.*", message = "Profile avatar must be an image data URL")
    private String profileAvatarDataUrl;
    private AppUserRole role;
}
