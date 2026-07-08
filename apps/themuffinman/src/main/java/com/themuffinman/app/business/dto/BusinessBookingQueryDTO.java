package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessBookingStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class BusinessBookingQueryDTO {
    private String q;
    private BusinessBookingStatus status;
    private Instant from;
    private Instant to;
    private Integer page;
    private Integer size;
}
