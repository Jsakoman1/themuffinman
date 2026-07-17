package com.themuffinman.app.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileGalleryImageRequestDTO {
    @NotBlank
    @Size(max = 500)
    private String imageUrl;

    @Size(max = 240)
    private String altText;

    private Integer sortOrder;
    private Boolean active;
}
