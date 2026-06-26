package com.themuffinman.app.location.dto;

import com.themuffinman.app.location.model.UserLocationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationModeOptionDTO {
    private UserLocationMode value;
    private String label;
    private String description;
}
