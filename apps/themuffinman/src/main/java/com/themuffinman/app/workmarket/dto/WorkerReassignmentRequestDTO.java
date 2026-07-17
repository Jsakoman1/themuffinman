package com.themuffinman.app.workmarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerReassignmentRequestDTO {

    @NotNull
    private Long replacementApplicationId;
}
