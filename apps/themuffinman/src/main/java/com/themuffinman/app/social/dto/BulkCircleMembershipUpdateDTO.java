package com.themuffinman.app.social.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCircleMembershipUpdateDTO {
    @NotNull
    private Long circleId;

    @NotEmpty
    private List<Long> userIds;

    @NotNull
    private BulkCircleMembershipActionDTO action;
}
