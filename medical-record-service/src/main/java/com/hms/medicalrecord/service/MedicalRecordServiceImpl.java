package com.hms.medicalrecord.service;

import com.hms.medicalrecord.client.AppointmentClient;
import com.hms.medicalrecord.client.AppointmentClientResponse;
import com.hms.medicalrecord.dto.ApiResponse;
import com.hms.medicalrecord.dto.MedicalRecordRequest;
import com.hms.medicalrecord.dto.MedicalRecordResponse;
import com.hms.medicalrecord.dto.MedicalRecordUpdateRequest;
import com.hms.medicalrecord.entity.MedicalRecord;
import com.hms.medicalrecord.exception.AppointmentNotCompletedException;
import com.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.hms.medicalrecord.exception.ValidationException;
import com.hms.medicalrecord.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentClient appointmentClient;

    @Value("${file.upload-dir:uploads/lab-reports}")
    private String uploadDir;

    @Override
    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request) {
        if (medicalRecordRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new ValidationException("Medical record already exists for this appointment");
        }

        try {
            ApiResponse<AppointmentClientResponse> response = appointmentClient.getAppointmentById(String.valueOf(request.getAppointmentId()));
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new ValidationException("Appointment not found");
            }
            AppointmentClientResponse appointmentData = response.getData();

            if (!"COMPLETED".equalsIgnoreCase(appointmentData.getStatus())) {
                throw new AppointmentNotCompletedException("Medical records can only be created for COMPLETED appointments");
            }

            if (!appointmentData.getPatientId().equals(request.getPatientId()) || 
                !appointmentData.getDoctorId().equals(request.getDoctorId())) {
                throw new ValidationException("Appointment does not match the provided Doctor or Patient ID");
            }

        } catch (AppointmentNotCompletedException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to validate appointment: " + e.getMessage());
        }

        MedicalRecord record = MedicalRecord.builder()
                .recordNumber(generateRecordNumber())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentId(request.getAppointmentId())
                .diagnosis(request.getDiagnosis())
                .symptoms(request.getSymptoms())
                .prescription(request.getPrescription())
                .allergies(request.getAllergies())
                .medicalHistory(request.getMedicalHistory())
                .notes(request.getNotes())
                .build();

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        return mapToResponse(savedRecord);
    }

    @Override
    public MedicalRecordResponse getMedicalRecordById(String recordNumber) {
        MedicalRecord record = medicalRecordRepository.findByRecordNumber(recordNumber)
                .orElseThrow(() -> new MedicalRecordNotFoundException("Medical record not found"));
        return mapToResponse(record);
    }

    @Override
    public List<MedicalRecordResponse> getPatientMedicalHistory(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicalRecordResponse updateMedicalRecord(String recordNumber, MedicalRecordUpdateRequest request) {
        MedicalRecord record = medicalRecordRepository.findByRecordNumber(recordNumber)
                .orElseThrow(() -> new MedicalRecordNotFoundException("Medical record not found"));

        if (request.getDiagnosis() != null) record.setDiagnosis(request.getDiagnosis());
        if (request.getSymptoms() != null) record.setSymptoms(request.getSymptoms());
        if (request.getPrescription() != null) record.setPrescription(request.getPrescription());
        if (request.getAllergies() != null) record.setAllergies(request.getAllergies());
        if (request.getMedicalHistory() != null) record.setMedicalHistory(request.getMedicalHistory());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        MedicalRecord updatedRecord = medicalRecordRepository.save(record);
        return mapToResponse(updatedRecord);
    }

    @Override
    @Transactional
    public MedicalRecordResponse uploadLabReport(String recordNumber, MultipartFile file) {
        MedicalRecord record = medicalRecordRepository.findByRecordNumber(recordNumber)
                .orElseThrow(() -> new MedicalRecordNotFoundException("Medical record not found"));

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String newFileName = recordNumber + "_" + System.currentTimeMillis() + "_" + fileName;
            Path filePath = uploadPath.resolve(newFileName);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            record.setLabReportUrl(filePath.toString());
            MedicalRecord updatedRecord = medicalRecordRepository.save(record);
            
            return mapToResponse(updatedRecord);
            
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    private String generateRecordNumber() {
        long count = medicalRecordRepository.count() + 1;
        return String.format("MR%06d", count);
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .recordNumber(record.getRecordNumber())
                .patientId(record.getPatientId())
                .doctorId(record.getDoctorId())
                .appointmentId(record.getAppointmentId())
                .diagnosis(record.getDiagnosis())
                .symptoms(record.getSymptoms())
                .prescription(record.getPrescription())
                .allergies(record.getAllergies())
                .medicalHistory(record.getMedicalHistory())
                .labReportUrl(record.getLabReportUrl())
                .notes(record.getNotes())
                .build();
    }
}
