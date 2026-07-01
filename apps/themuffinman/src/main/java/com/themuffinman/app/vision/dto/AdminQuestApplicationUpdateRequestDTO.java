package com.themuffinman.app.vision.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
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
public class AdminQuestApplicationUpdateRequestDTO {
    @ContractOptional
    @Nullable
    @Size(max = 2000)
    private String message;

    @ContractOptional
    @Nullable
    @Digits(integer = 8, fraction = 2)
    private BigDecimal proposedPrice;

    @ContractOptional
    @Nullable
    private QuestApplicationStatus status;
}
