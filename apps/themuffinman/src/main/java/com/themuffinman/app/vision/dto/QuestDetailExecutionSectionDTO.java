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
public class QuestDetailExecutionSectionDTO {
    private boolean visible;
    @Nullable
    private QuestDetailExecutionActionDTO primaryAction;
    @Nullable
    private String primaryActionLabel;
    @Nullable
    private String helperText;
}
