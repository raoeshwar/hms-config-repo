package com.hms.doctor.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class DoctorAvailabilityResponse {
    private String doctorCode;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private Boolean isAvailableNow;
}
