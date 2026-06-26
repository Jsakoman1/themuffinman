package com.themuffinman.app.chat.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactDTO {
    private Long userId;
    private String username;
    @Nullable
    private String profileDescription;
    @Nullable
    private String profileAvatarDataUrl;
    private List<Long> circleIds;
    private List<String> circleNames;
    private boolean online;
    @Nullable
    private String lastActiveAt;
}
