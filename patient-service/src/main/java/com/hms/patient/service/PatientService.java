package com.hms.patient.service;

import com.hms.patient.dto.PatientRequest;
import com.hms.patient.dto.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PatientService {
    PatientResponse addPatient(PatientRequest request);
    PatientResponse updatePatient(String patientId, PatientRequest request);
    PatientResponse getPatientById(String patientId);
    Page<PatientResponse> getAllPatients(Pageable pageable);
    List<PatientResponse> searchPatients(String name, String phone, String email, String bloodGroup, String city);
    void deletePatient(String patientId);
    PatientResponse getPatientProfile(String email);
}
