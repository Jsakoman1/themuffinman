package com.themuffinman.app.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleGroupRequestDTO {
    @NotBlank(message = "Circle name is required")
    @Size(max = 80, message = "Circle name must be 80 characters or less")
    private String name;
}
