package com.sidequest.sidequest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReviewRequestDTO {
    private Long reviewedUserId;
    private Integer stars;
    private String comment;
}
