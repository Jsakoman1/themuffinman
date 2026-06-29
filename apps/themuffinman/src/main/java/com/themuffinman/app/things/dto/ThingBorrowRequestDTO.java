package com.themuffinman.app.things.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThingBorrowRequestDTO {
    @Size(max = 1000, message = "Borrow request message must be 1000 characters or less")
    private String message;
}
