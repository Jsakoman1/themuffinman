package com.themuffinman.app.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationTargetDTO {
    private NavigationTargetType type;
    @Nullable
    private Long entityId;
}
