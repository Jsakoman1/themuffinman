package com.themuffinman.app.vision.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class UserReviewRequestDTO {
    private Long reviewedUserId;
    private Integer stars;
    @ContractOptional
    @Nullable
    private String comment;
}
