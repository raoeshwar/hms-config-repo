package com.hms.patient.service;

import com.hms.patient.dto.PatientRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.DuplicateEmailException;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addPatient_Success() {
        PatientRequest request = new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@gmail.com");
        request.setPhone("1234567890");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.existsByPhone(anyString())).thenReturn(false);
        when(patientRepository.count()).thenReturn(0L);

        Patient savedPatient = new Patient();
        savedPatient.setPatientCode("PAT000001");
        savedPatient.setFirstName("John");
        savedPatient.setEmail("john.doe@gmail.com");

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponse response = patientService.addPatient(request);

        assertNotNull(response);
        assertEquals("PAT000001", response.getPatientId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void addPatient_DuplicateEmail() {
        PatientRequest request = new PatientRequest();
        request.setEmail("john.doe@gmail.com");

        when(patientRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> patientService.addPatient(request));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getPatientById_Success() {
        Patient patient = new Patient();
        patient.setPatientCode("PAT000001");
        patient.setFirstName("John");

        when(patientRepository.findByPatientCodeAndStatusTrue("PAT000001")).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getPatientById("PAT000001");

        assertNotNull(response);
        assertEquals("PAT000001", response.getPatientId());
    }

    @Test
    void getPatientById_NotFound() {
        when(patientRepository.findByPatientCodeAndStatusTrue(anyString())).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> patientService.getPatientById("PAT000001"));
    }

    @Test
    void deletePatient_Success() {
        Patient patient = new Patient();
        patient.setPatientCode("PAT000001");
        patient.setStatus(true);

        when(patientRepository.findByPatientCodeAndStatusTrue("PAT000001")).thenReturn(Optional.of(patient));

        assertDoesNotThrow(() -> patientService.deletePatient("PAT000001"));
        assertFalse(patient.getStatus());
        verify(patientRepository, times(1)).save(patient);
    }
}
