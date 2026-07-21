package com.hms.appointment.dto;

import lombok.Data;

@Data
public class PatientClientResponse {
    private String patientId; // The PATXXXX code
    private String firstName;
    private String lastName;
}
