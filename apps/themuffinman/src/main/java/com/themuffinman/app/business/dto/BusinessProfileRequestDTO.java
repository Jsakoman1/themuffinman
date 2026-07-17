package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class BusinessProfileRequestDTO {

    @NotBlank(message = "Business name is required")
    @Size(max = 120, message = "Business name must be 120 characters or less")
    private String businessName;

    @Pattern(regexp = "^(?:[a-z0-9]+(?:-[a-z0-9]+)*)?$", message = "Business slug must use lowercase letters, numbers, and hyphens")
    @Size(max = 140, message = "Business slug must be 140 characters or less")
    private String slug;

    @Size(max = 160, message = "Business headline must be 160 characters or less")
    private String headline;

    @Size(max = 4000, message = "Business description must be 4000 characters or less")
    private String description;

    @Email(message = "Business contact email must be valid")
    @Size(max = 160, message = "Business contact email must be 160 characters or less")
    private String contactEmail;

    @Size(max = 80, message = "Business contact phone must be 80 characters or less")
    private String contactPhone;

    @Size(max = 300, message = "Business website URL must be 300 characters or less")
    private String websiteUrl;

    @Size(max = 80, message = "Business timezone must be 80 characters or less")
    private String timezone;

    private Boolean bookingEnabled;

    @Size(max = 240, message = "Business address label must be 240 characters or less")
    private String publicAddressLabel;

    private Double latitude;

    private Double longitude;

    @Size(max = 80, message = "Business WhatsApp contact must be 80 characters or less")
    private String contactWhatsapp;

    @Size(max = 500, message = "Business hero image URL must be 500 characters or less")
    private String heroImageUrl;

    private Boolean active;
}
