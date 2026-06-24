package com.themuffinman.app.workmarket.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationRequestDTO {

    private @NotBlank(message = "Application message is required") @Size(max = 2000, message = "Application message must be 2000 characters or less") String message;
    private @NotNull(message = "Proposed price is required") @DecimalMin(value = "0.01", message = "Proposed price must be at least 0.01") @Digits(integer = 8, fraction = 2) BigDecimal proposedPrice;
}
