package com.hms.doctor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailabilityRequest {
    @NotNull(message = "Available From is required")
    private LocalTime availableFrom;

    @NotNull(message = "Available To is required")
    private LocalTime availableTo;
}
