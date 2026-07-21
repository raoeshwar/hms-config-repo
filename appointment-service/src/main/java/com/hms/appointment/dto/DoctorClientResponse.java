package com.hms.appointment.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class DoctorClientResponse {
    private String doctorCode;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private Boolean isAvailableNow;
    private Boolean status;
}
