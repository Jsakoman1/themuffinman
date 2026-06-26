package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPlannerItemDTO {
    private String id;
    private Long questId;
    private String title;
    @Nullable
    private NavigationTargetDTO navigation;
    @Nullable
    private Instant scheduledAt;
    @Nullable
    private Instant endsAt;
    private String kind;
    private String kindLabel;
    private String tone;
    private boolean hasRange;
}
