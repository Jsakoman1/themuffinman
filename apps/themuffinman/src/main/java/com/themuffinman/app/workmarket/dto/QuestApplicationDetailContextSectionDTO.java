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
public class QuestApplicationDetailContextSectionDTO {
    @Nullable
    private String questLabel;
    @Nullable
    private String postedByLabel;
    private boolean showStatus;
    private boolean showTerm;
    private boolean showWorkers;
}
