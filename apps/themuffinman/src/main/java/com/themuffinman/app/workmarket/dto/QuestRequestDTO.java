package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRequestDTO {
    private static final int QUEST_IMAGE_MAX_LENGTH = 350000;

    private @NotBlank(message = "Quest title is required") @Size(max = 255, message = "Quest title must be 255 characters or less") String title;
    @ContractOptional
    @Nullable
    private Long resourceVersion;
    private @NotBlank(message = "Quest description is required") @Size(max = 2000, message = "Quest description must be 2000 characters or less") String description;
    private @NotNull(message = "Award amount is required") @DecimalMin(value = "0.00", message = "Award amount cannot be negative") @Digits(integer = 8, fraction = 2) BigDecimal awardAmount;
    @ContractOptional
    @Nullable
    private @Positive Integer assigneeTarget;
    @ContractOptional
    @Nullable
    private Boolean showApprovedApplicants;
    @ContractOptional
    @Nullable
    private Instant scheduledAt;
    @ContractOptional
    @Nullable
    private Instant endsAt;
    @ContractOptional
    @Nullable
    private Boolean termFixed;
    @ContractOptional
    private QuestAudience audience;
    @ContractOptional
    private List<@Positive Long> selectedCircleIds;
    @ContractOptional
    private @Positive Long creatorId;
    @ContractOptional
    private QuestStatus status;
    @ContractOptional
    private @Size(max = 10, message = "A quest can have at most 10 images") List<
            @NotBlank(message = "Quest images must not be empty")
            @Size(max = QUEST_IMAGE_MAX_LENGTH, message = "Quest images must be 350000 characters or less")
            @Pattern(regexp = "^data:image/.*", message = "Quest images must be image data URLs")
            String
            > images;
    @ContractOptional
    @Nullable
    private QuestLocationVisibility locationVisibility;
    @ContractOptional
    @Nullable
    private QuestLocationSource locationSource;
    @ContractOptional
    @Nullable
    private String locationLabel;
    @ContractOptional
    @Nullable
    private String locationCountryCode;
    @ContractOptional
    @Nullable
    private String locationCountry;
    @ContractOptional
    @Nullable
    private String locationLocality;
    @ContractOptional
    @Nullable
    private String locationPostalCode;
    @ContractOptional
    @Nullable
    private String locationStreet;
    @ContractOptional
    @Nullable
    private String locationHouseNumber;
}
