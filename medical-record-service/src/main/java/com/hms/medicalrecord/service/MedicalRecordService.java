package com.hms.medicalrecord.service;

import com.hms.medicalrecord.dto.MedicalRecordRequest;
import com.hms.medicalrecord.dto.MedicalRecordResponse;
import com.hms.medicalrecord.dto.MedicalRecordUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request);
    MedicalRecordResponse getMedicalRecordById(String recordNumber);
    List<MedicalRecordResponse> getPatientMedicalHistory(Long patientId);
    MedicalRecordResponse updateMedicalRecord(String recordNumber, MedicalRecordUpdateRequest request);
    MedicalRecordResponse uploadLabReport(String recordNumber, MultipartFile file);
}
