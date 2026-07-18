package com.themuffinman.app.identity.dto;
import lombok.Builder;
@Builder public record PersonalShortcutResponseDTO(long targetId, String targetType, String title, String route) {}
