package com.sidequest.sidequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

import com.sidequest.sidequest.model.QuestAudience;
import com.sidequest.sidequest.model.QuestStatus;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRequestDTO {

    private @NotBlank @Size(max = 255) String title;
    private @NotBlank @Size(max = 2000) String description;
    private @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal awardAmount;
    private @Positive Integer assigneeTarget;
    private Instant scheduledAt;
    private Instant endsAt;
    private Boolean termFixed;
    private QuestAudience audience;
    private List<@Positive Long> selectedCircleIds;
    private @Positive Long creatorId;
    private QuestStatus status;
    private @Size(max = 10) List<@NotBlank @Size(max = 12000) @Pattern(regexp = "^data:image/.*", message = "Quest images must be image data URLs") String> images;
}
