package com.themuffinman.app.location.dto;

import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExactLocationVisibilityScopeOptionDTO {
    private ExactLocationVisibilityScope value;
    private String label;
    private String description;
}
