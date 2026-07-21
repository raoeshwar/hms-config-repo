package com.hms.appointment.controller;

import com.hms.appointment.dto.ApiResponse;
import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully.", response));
    }

    @GetMapping("/{appointmentNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable String appointmentNumber) {
        AppointmentResponse response = appointmentService.getAppointmentById(appointmentNumber);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved successfully.", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointments(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date) {
        Page<AppointmentResponse> response = appointmentService.getAllAppointments(pageable, status, date);
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully.", response));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPatientAppointments(@PathVariable Long patientId) {
        List<AppointmentResponse> response = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient appointments retrieved successfully.", response));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<AppointmentResponse> response = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Doctor appointments retrieved successfully.", response));
    }

    @PutMapping("/{appointmentNumber}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable String appointmentNumber,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(appointmentNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled successfully.", response));
    }

    @PutMapping("/{appointmentNumber}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(@PathVariable String appointmentNumber) {
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentNumber);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully.", response));
    }

    @PutMapping("/{appointmentNumber}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable String appointmentNumber,
            @RequestBody Map<String, String> statusBody) {
        String status = statusBody.get("status");
        AppointmentResponse response = appointmentService.updateAppointmentStatus(appointmentNumber, status);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated successfully.", response));
    }
}
