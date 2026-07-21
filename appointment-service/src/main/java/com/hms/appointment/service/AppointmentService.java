package com.hms.appointment.service;

import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentRequest request);
    AppointmentResponse getAppointmentById(String appointmentNumber);
    Page<AppointmentResponse> getAllAppointments(Pageable pageable, String status, String date);
    List<AppointmentResponse> getPatientAppointments(Long patientId);
    List<AppointmentResponse> getDoctorAppointments(Long doctorId);
    AppointmentResponse rescheduleAppointment(String appointmentNumber, AppointmentRequest request);
    AppointmentResponse cancelAppointment(String appointmentNumber);
    AppointmentResponse updateAppointmentStatus(String appointmentNumber, String status);
}
