package com.themuffinman.app.workmarket.dto;

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
public class QuestDetailTermChangeSectionDTO {
    private boolean visible;
    private boolean actionable;
    private String summaryLabel;
    private String confirmLabel;
    private String rejectLabel;
    @Nullable
    private Instant currentScheduledAt;
    @Nullable
    private Instant currentEndsAt;
    private boolean currentTermFixed;
    @Nullable
    private Instant pendingScheduledAt;
    @Nullable
    private Instant pendingEndsAt;
    @Nullable
    private Boolean pendingTermFixed;
}
