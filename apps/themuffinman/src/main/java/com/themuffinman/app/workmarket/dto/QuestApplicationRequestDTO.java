package com.themuffinman.app.workmarket.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationRequestDTO {
    private @NotBlank(message = "Application message is required") @Size(max = 2000, message = "Application message must be 2000 characters or less") String message;
    @Nullable
    private @Digits(integer = 8, fraction = 2) BigDecimal proposedPrice;
}
