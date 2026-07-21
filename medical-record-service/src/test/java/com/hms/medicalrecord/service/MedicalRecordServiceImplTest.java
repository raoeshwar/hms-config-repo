package com.hms.medicalrecord.service;

import com.hms.medicalrecord.client.AppointmentClient;
import com.hms.medicalrecord.client.AppointmentClientResponse;
import com.hms.medicalrecord.dto.ApiResponse;
import com.hms.medicalrecord.dto.MedicalRecordRequest;
import com.hms.medicalrecord.dto.MedicalRecordResponse;
import com.hms.medicalrecord.entity.MedicalRecord;
import com.hms.medicalrecord.exception.AppointmentNotCompletedException;
import com.hms.medicalrecord.exception.ValidationException;
import com.hms.medicalrecord.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(medicalRecordService, "uploadDir", "test-uploads");
    }

    @Test
    void createMedicalRecord_Success() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setPatientId(101L);
        request.setDoctorId(25L);
        request.setAppointmentId(5001L);
        request.setDiagnosis("Viral Fever");
        request.setPrescription("Paracetamol");

        AppointmentClientResponse appointmentResponse = new AppointmentClientResponse();
        appointmentResponse.setAppointmentNumber("APT000001");
        appointmentResponse.setPatientId(101L);
        appointmentResponse.setDoctorId(25L);
        appointmentResponse.setStatus("COMPLETED");

        when(medicalRecordRepository.existsByAppointmentId(5001L)).thenReturn(false);
        when(appointmentClient.getAppointmentById("5001"))
                .thenReturn(ApiResponse.success("Success", appointmentResponse));

        MedicalRecord savedRecord = MedicalRecord.builder()
                .recordNumber("MR000001")
                .patientId(101L)
                .doctorId(25L)
                .appointmentId(5001L)
                .diagnosis("Viral Fever")
                .prescription("Paracetamol")
                .build();

        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(savedRecord);
        when(medicalRecordRepository.count()).thenReturn(0L);

        MedicalRecordResponse response = medicalRecordService.createMedicalRecord(request);

        assertNotNull(response);
        assertEquals("MR000001", response.getRecordNumber());
        assertEquals("Viral Fever", response.getDiagnosis());
    }

    @Test
    void createMedicalRecord_AppointmentNotCompleted_ThrowsException() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setPatientId(101L);
        request.setDoctorId(25L);
        request.setAppointmentId(5001L);

        AppointmentClientResponse appointmentResponse = new AppointmentClientResponse();
        appointmentResponse.setPatientId(101L);
        appointmentResponse.setDoctorId(25L);
        appointmentResponse.setStatus("BOOKED");

        when(medicalRecordRepository.existsByAppointmentId(5001L)).thenReturn(false);
        when(appointmentClient.getAppointmentById("5001"))
                .thenReturn(ApiResponse.success("Success", appointmentResponse));

        assertThrows(AppointmentNotCompletedException.class, () -> medicalRecordService.createMedicalRecord(request));
    }
}
