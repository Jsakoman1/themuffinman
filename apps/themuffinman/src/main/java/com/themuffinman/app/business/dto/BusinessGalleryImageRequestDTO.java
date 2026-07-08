package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessGalleryImageRequestDTO {

    @NotBlank(message = "Image url is required")
    @Size(max = 500, message = "Image url must be 500 characters or fewer")
    private String imageUrl;

    @Size(max = 240, message = "Alt text must be 240 characters or fewer")
    private String altText;

    private Integer sortOrder;

    private Boolean active;
}
