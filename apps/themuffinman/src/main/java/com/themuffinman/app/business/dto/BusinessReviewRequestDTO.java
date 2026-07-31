package com.themuffinman.app.business.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class BusinessReviewRequestDTO {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer stars;

    @ContractOptional
    @Nullable
    @Size(max = 2000)
    private String comment;
}
