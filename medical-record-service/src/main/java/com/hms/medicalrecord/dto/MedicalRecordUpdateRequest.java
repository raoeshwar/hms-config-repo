package com.hms.medicalrecord.dto;

import lombok.Data;

@Data
public class MedicalRecordUpdateRequest {
    private String diagnosis;
    private String symptoms;
    private String prescription;
    private String allergies;
    private String medicalHistory;
    private String notes;
}
