package com.themuffinman.app.identity.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;

import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserResponseDTO {
    private Long id;
    private String email;
    private String username;
    private NavigationTargetDTO profileNavigation;
    @Nullable
    private String profileDescription;
    @Nullable
    private String profileAvatarDataUrl;
    private Instant createdAt;
    private long openQuestCount;
    private List<QuestResponseDTO> openQuests;
    private String role;
}
