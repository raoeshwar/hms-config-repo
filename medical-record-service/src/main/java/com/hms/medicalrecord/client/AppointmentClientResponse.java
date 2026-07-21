package com.hms.medicalrecord.client;

import lombok.Data;

@Data
public class AppointmentClientResponse {
    private String appointmentNumber;
    private Long patientId;
    private Long doctorId;
    private String status;
}
