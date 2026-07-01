package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationDraftRulesViewDTO {
    private boolean messageRequired;
    private boolean proposedPriceRequired;
    @Nullable
    private BigDecimal minimumProposedPrice;
    @Nullable
    private BigDecimal suggestedApplicationPrice;
}
