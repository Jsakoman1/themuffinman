package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDetailReviewSectionDTO {
    private boolean visible;
    private boolean canSubmit;
    private String introTitle;
    @Nullable
    private String introSubtitle;
    private String placeholder;
    private String submitLabel;
    private String emptyStateMessage;
    @Nullable
    private QuestDetailReviewTargetDTO target;
    @Nullable
    private UserReviewResponseDTO submittedReview;
}
