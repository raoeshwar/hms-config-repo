package com.hms.patient.controller;

import com.hms.patient.dto.ApiResponse;
import com.hms.patient.dto.PatientRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.exception.ForbiddenException;
import com.hms.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> addPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.addPatient(request);
        return ResponseEntity.ok(ApiResponse.success("Patient registered successfully.", response));
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable String patientId,
            @Valid @RequestBody PatientRequest request,
            Authentication authentication) {
        
        checkPatientOwnership(patientId, authentication);

        PatientResponse response = patientService.updatePatient(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully.", response));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @PathVariable String patientId,
            Authentication authentication) {
        
        checkPatientOwnership(patientId, authentication);

        PatientResponse response = patientService.getPatientById(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully.", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> getAllPatients(Pageable pageable) {
        Page<PatientResponse> response = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved successfully.", response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> searchPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city) {
        
        List<PatientResponse> response = patientService.searchPatients(name, phone, email, bloodGroup, city);
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved successfully.", response));
    }

    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable String patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully."));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientProfile(Authentication authentication) {
        String email = authentication.getName();
        PatientResponse response = patientService.getPatientProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully.", response));
    }

    private void checkPatientOwnership(String patientId, Authentication authentication) {
        boolean isPatientRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PATIENT"));

        if (isPatientRole) {
            String loggedInEmail = authentication.getName();
            PatientResponse profile = patientService.getPatientProfile(loggedInEmail);
            if (!profile.getPatientId().equals(patientId)) {
                throw new ForbiddenException("You can only access or update your own profile.");
            }
        }
    }
}
