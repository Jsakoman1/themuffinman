package com.themuffinman.app.rides.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideOfferRequestDTO {
    @NotBlank(message = "Ride origin is required")
    @Size(max = 140, message = "Ride origin must be 140 characters or less")
    private String origin;

    @NotBlank(message = "Ride destination is required")
    @Size(max = 140, message = "Ride destination must be 140 characters or less")
    private String destination;

    @NotNull(message = "Ride departure time is required")
    private Instant departureAt;

    @NotNull(message = "Seat count is required")
    @Min(value = 1, message = "Seat count must be at least 1")
    @Max(value = 8, message = "Seat count must be 8 or less")
    private Integer seats;

    @Size(max = 1000, message = "Ride note must be 1000 characters or less")
    private String note;

    private Boolean active;

    private List<@Positive Long> visibleCircleIds;
}
