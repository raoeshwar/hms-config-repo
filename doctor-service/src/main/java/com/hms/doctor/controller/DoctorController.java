package com.hms.doctor.controller;

import com.hms.doctor.dto.ApiResponse;
import com.hms.doctor.dto.AvailabilityRequest;
import com.hms.doctor.dto.DoctorAvailabilityResponse;
import com.hms.doctor.dto.DoctorRequest;
import com.hms.doctor.dto.DoctorResponse;
import com.hms.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> registerDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.registerDoctor(request);
        return ResponseEntity.ok(ApiResponse.success("Doctor registered successfully", response));
    }

    @PutMapping("/{doctorCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable String doctorCode, 
            @Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.updateDoctor(doctorCode, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", response));
    }

    @GetMapping("/{doctorCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctor(@PathVariable String doctorCode) {
        DoctorResponse response = doctorService.getDoctor(doctorCode);
        return ResponseEntity.ok(ApiResponse.success("Doctor retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        List<DoctorResponse> response = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", response));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAvailableDoctors() {
        List<DoctorResponse> response = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(ApiResponse.success("Available doctors retrieved successfully", response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> searchDoctors(@RequestParam(required = false) String query) {
        List<DoctorResponse> response = doctorService.searchDoctors(query);
        return ResponseEntity.ok(ApiResponse.success("Doctors searched successfully", response));
    }

    @DeleteMapping("/{doctorCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disableDoctor(@PathVariable String doctorCode) {
        doctorService.disableDoctor(doctorCode);
        return ResponseEntity.ok(ApiResponse.success("Doctor disabled successfully"));
    }

    @GetMapping("/availability/{doctorCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> getDoctorAvailability(@PathVariable String doctorCode) {
        DoctorAvailabilityResponse response = doctorService.getDoctorAvailability(doctorCode);
        return ResponseEntity.ok(ApiResponse.success("Doctor availability retrieved successfully", response));
    }

    @PutMapping("/{doctorCode}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> updateDoctorAvailability(
            @PathVariable String doctorCode,
            @Valid @RequestBody AvailabilityRequest request) {
        DoctorAvailabilityResponse response = doctorService.updateDoctorAvailability(doctorCode, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor availability updated successfully", response));
    }
}
