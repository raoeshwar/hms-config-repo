package com.hms.medicalrecord.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedicalRecordResponse {
    private String recordNumber;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private String diagnosis;
    private String symptoms;
    private String prescription;
    private String allergies;
    private String medicalHistory;
    private String labReportUrl;
    private String notes;
}
