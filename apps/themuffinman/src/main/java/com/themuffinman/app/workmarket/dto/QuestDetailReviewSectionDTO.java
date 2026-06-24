package com.themuffinman.app.workmarket.dto;

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
    private String placeholder;
    @Nullable
    private QuestDetailReviewTargetDTO target;
    @Nullable
    private UserReviewResponseDTO submittedReview;
}
