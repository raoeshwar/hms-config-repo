package com.hms.appointment.service;

import com.hms.appointment.client.DoctorClient;
import com.hms.appointment.client.PatientClient;
import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.dto.ApiResponse;
import com.hms.appointment.dto.DoctorClientResponse;
import com.hms.appointment.dto.PatientClientResponse;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentStatus;
import com.hms.appointment.exception.*;
import com.hms.appointment.kafka.AppointmentEvent;
import com.hms.appointment.kafka.AppointmentEventProducer;
import com.hms.appointment.repository.AppointmentRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorClient doctorClient;
    private final PatientClient patientClient;
    private final AppointmentEventProducer kafkaProducer;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        validateTime(request);
        validatePatientAndDoctor(request);

        if (appointmentRepository.existsConflictingAppointment(
                request.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime())) {
            throw new AppointmentConflictException("Doctor is not available for the selected time slot.");
        }

        Appointment appointment = Appointment.builder()
                .appointmentNumber(generateAppointmentNumber())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.BOOKED)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        publishEvent("appointment-created", savedAppointment);

        return mapToResponse(savedAppointment);
    }

    @Override
    public AppointmentResponse getAppointmentById(String appointmentNumber) {
        Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        return mapToResponse(appointment);
    }

    @Override
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable, String status, String date) {
        Specification<Appointment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), AppointmentStatus.valueOf(status.toUpperCase())));
            }
            if (date != null && !date.isEmpty()) {
                predicates.add(cb.equal(root.get("appointmentDate"), LocalDate.parse(date)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return appointmentRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(String appointmentNumber, AppointmentRequest request) {
        validateTime(request);

        Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new InvalidAppointmentStatusException("Only BOOKED appointments can be rescheduled");
        }

        validatePatientAndDoctor(request);

        if (appointmentRepository.existsConflictingAppointment(
                request.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime())) {
            throw new AppointmentConflictException("Doctor is not available for the selected time slot.");
        }

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setReason(request.getReason());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        publishEvent("appointment-rescheduled", updatedAppointment);

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(String appointmentNumber) {
        Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStatusException("Completed appointments cannot be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        publishEvent("appointment-cancelled", updatedAppointment);

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(String appointmentNumber, String status) {
        Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStatusException("Cancelled appointments cannot be updated");
        }

        AppointmentStatus newStatus = AppointmentStatus.valueOf(status.toUpperCase());
        appointment.setStatus(newStatus);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        if (newStatus == AppointmentStatus.COMPLETED) {
            publishEvent("appointment-completed", updatedAppointment);
        }

        return mapToResponse(updatedAppointment);
    }

    private void validateTime(AppointmentRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
            throw new ValidationException("End Time must be after Start Time");
        }
    }

    private void validatePatientAndDoctor(AppointmentRequest request) {
        try {
            ApiResponse<PatientClientResponse> patientRes = patientClient.getPatientById(request.getPatientId());
            if (patientRes == null || !patientRes.isSuccess()) {
                throw new ValidationException("Patient not found");
            }
        } catch (Exception e) {
            throw new ValidationException("Patient not found or service unavailable");
        }

        try {
            ApiResponse<DoctorClientResponse> docRes = doctorClient.getDoctorAvailability(request.getDoctorId());
            if (docRes == null || !docRes.isSuccess()) {
                throw new ValidationException("Doctor not found");
            }
            DoctorClientResponse doctorData = docRes.getData();
            if (doctorData.getStatus() != null && !doctorData.getStatus()) {
                throw new DoctorUnavailableException("Doctor is currently inactive");
            }
            if (request.getStartTime().isBefore(doctorData.getAvailableFrom()) || request.getEndTime().isAfter(doctorData.getAvailableTo())) {
                throw new DoctorUnavailableException("Requested time is outside doctor's working hours");
            }
        } catch (DoctorUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Doctor not found or service unavailable");
        }
    }

    private String generateAppointmentNumber() {
        long count = appointmentRepository.count() + 1;
        return String.format("APT%06d", count);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentNumber(appointment.getAppointmentNumber())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .build();
    }
    
    private void publishEvent(String topic, Appointment appointment) {
        AppointmentEvent event = AppointmentEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(topic)
                .appointmentNumber(appointment.getAppointmentNumber())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .status(appointment.getStatus().name())
                .build();
        kafkaProducer.publishAppointmentEvent(topic, event);
    }
}
