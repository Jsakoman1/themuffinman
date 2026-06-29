package com.themuffinman.app.things.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThingListingRequestDTO {
    @NotBlank(message = "Thing title is required")
    @Size(max = 140, message = "Thing title must be 140 characters or less")
    private String title;

    @Size(max = 2000, message = "Thing description must be 2000 characters or less")
    private String description;

    @Size(max = 180, message = "Condition note must be 180 characters or less")
    private String conditionNote;

    private Boolean available;
}
