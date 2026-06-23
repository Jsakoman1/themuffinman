package com.sidequest.sidequest.dto;

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

    private @NotBlank @Size(max = 2000) String message;
    private @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal proposedPrice;
}
