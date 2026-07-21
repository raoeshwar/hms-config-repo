package com.hms.medicalrecord.controller;

import com.hms.medicalrecord.dto.ApiResponse;
import com.hms.medicalrecord.dto.MedicalRecordRequest;
import com.hms.medicalrecord.dto.MedicalRecordResponse;
import com.hms.medicalrecord.dto.MedicalRecordUpdateRequest;
import com.hms.medicalrecord.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecordResponse response = medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.ok(ApiResponse.success("Medical record created successfully.", response));
    }

    @GetMapping("/{recordNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordById(
            @PathVariable String recordNumber) {
        MedicalRecordResponse response = medicalRecordService.getMedicalRecordById(recordNumber);
        return ResponseEntity.ok(ApiResponse.success("Medical record retrieved successfully.", response));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getPatientMedicalHistory(
            @PathVariable Long patientId) {
        List<MedicalRecordResponse> response = medicalRecordService.getPatientMedicalHistory(patientId);
        return ResponseEntity.ok(ApiResponse.success("Medical records retrieved successfully.", response));
    }

    @PutMapping("/{recordNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateMedicalRecord(
            @PathVariable String recordNumber,
            @RequestBody MedicalRecordUpdateRequest request) {
        MedicalRecordResponse response = medicalRecordService.updateMedicalRecord(recordNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Medical record updated successfully.", response));
    }

    @PostMapping(value = "/{recordNumber}/lab-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> uploadLabReport(
            @PathVariable String recordNumber,
            @RequestParam("file") MultipartFile file) {
        MedicalRecordResponse response = medicalRecordService.uploadLabReport(recordNumber, file);
        return ResponseEntity.ok(ApiResponse.success("Lab report uploaded successfully.", response));
    }

    @GetMapping("/{recordNumber}/lab-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<Resource> downloadLabReport(@PathVariable String recordNumber) {
        MedicalRecordResponse record = medicalRecordService.getMedicalRecordById(recordNumber);
        
        if (record.getLabReportUrl() == null || record.getLabReportUrl().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path file = Paths.get(record.getLabReportUrl());
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
