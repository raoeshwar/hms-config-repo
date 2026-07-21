package com.hms.appointment.service;

import com.hms.appointment.client.DoctorClient;
import com.hms.appointment.client.PatientClient;
import com.hms.appointment.dto.ApiResponse;
import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.dto.DoctorClientResponse;
import com.hms.appointment.dto.PatientClientResponse;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentStatus;
import com.hms.appointment.exception.AppointmentConflictException;
import com.hms.appointment.kafka.AppointmentEventProducer;
import com.hms.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorClient doctorClient;
    @Mock
    private PatientClient patientClient;
    @Mock
    private AppointmentEventProducer kafkaProducer;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void bookAppointment_Success() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(101L);
        request.setDoctorId(15L);
        request.setAppointmentDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 30));
        request.setReason("Checkup");

        PatientClientResponse patientResponse = new PatientClientResponse();
        patientResponse.setPatientId("101");
        
        DoctorClientResponse doctorResponse = new DoctorClientResponse();
        doctorResponse.setDoctorCode("DOC015");
        doctorResponse.setStatus(true);
        doctorResponse.setAvailableFrom(LocalTime.of(9, 0));
        doctorResponse.setAvailableTo(LocalTime.of(17, 0));

        when(patientClient.getPatientById(101L))
                .thenReturn(ApiResponse.success("Success", patientResponse));
        when(doctorClient.getDoctorAvailability(15L))
                .thenReturn(ApiResponse.success("Success", doctorResponse));

        when(appointmentRepository.existsConflictingAppointment(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(false);

        Appointment savedAppointment = Appointment.builder()
                .appointmentNumber("APT000001")
                .status(AppointmentStatus.BOOKED)
                .build();
                
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);
        when(appointmentRepository.count()).thenReturn(0L);

        AppointmentResponse response = appointmentService.bookAppointment(request);

        assertNotNull(response);
        assertEquals("APT000001", response.getAppointmentNumber());
        assertEquals(AppointmentStatus.BOOKED, response.getStatus());
        verify(kafkaProducer, times(1)).publishAppointmentEvent(eq("appointment-created"), any());
    }

    @Test
    void bookAppointment_Conflict_ThrowsException() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(101L);
        request.setDoctorId(15L);
        request.setAppointmentDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 30));
        request.setReason("Checkup");

        PatientClientResponse patientResponse = new PatientClientResponse();
        DoctorClientResponse doctorResponse = new DoctorClientResponse();
        doctorResponse.setStatus(true);
        doctorResponse.setAvailableFrom(LocalTime.of(9, 0));
        doctorResponse.setAvailableTo(LocalTime.of(17, 0));

        when(patientClient.getPatientById(101L)).thenReturn(ApiResponse.success("Success", patientResponse));
        when(doctorClient.getDoctorAvailability(15L)).thenReturn(ApiResponse.success("Success", doctorResponse));

        when(appointmentRepository.existsConflictingAppointment(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(true);

        assertThrows(AppointmentConflictException.class, () -> appointmentService.bookAppointment(request));
    }
}
