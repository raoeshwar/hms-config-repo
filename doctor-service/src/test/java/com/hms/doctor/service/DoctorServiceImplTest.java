package com.hms.doctor.service;

import com.hms.doctor.dto.AvailabilityRequest;
import com.hms.doctor.dto.DoctorAvailabilityResponse;
import com.hms.doctor.dto.DoctorRequest;
import com.hms.doctor.dto.DoctorResponse;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.doctor.exception.DuplicateEmailException;
import com.hms.doctor.exception.ValidationException;
import com.hms.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerDoctor_Success() {
        DoctorRequest request = new DoctorRequest();
        request.setFirstName("Ramesh");
        request.setLastName("Kumar");
        request.setEmail("ramesh@gmail.com");
        request.setPhone("9876543210");
        request.setAvailableFrom(LocalTime.of(9, 0));
        request.setAvailableTo(LocalTime.of(17, 0));

        when(doctorRepository.existsByEmail(anyString())).thenReturn(false);
        when(doctorRepository.existsByPhone(anyString())).thenReturn(false);
        when(doctorRepository.count()).thenReturn(0L);

        Doctor savedDoctor = new Doctor();
        savedDoctor.setDoctorCode("DOC0001");
        savedDoctor.setFirstName("Ramesh");

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);

        DoctorResponse response = doctorService.registerDoctor(request);

        assertNotNull(response);
        assertEquals("DOC0001", response.getDoctorCode());
    }

    @Test
    void registerDoctor_InvalidTime_ThrowsValidationException() {
        DoctorRequest request = new DoctorRequest();
        request.setAvailableFrom(LocalTime.of(17, 0));
        request.setAvailableTo(LocalTime.of(9, 0));

        assertThrows(ValidationException.class, () -> doctorService.registerDoctor(request));
    }

    @Test
    void registerDoctor_DuplicateEmail_ThrowsException() {
        DoctorRequest request = new DoctorRequest();
        request.setAvailableFrom(LocalTime.of(9, 0));
        request.setAvailableTo(LocalTime.of(17, 0));
        request.setEmail("ramesh@gmail.com");

        when(doctorRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> doctorService.registerDoctor(request));
    }

    @Test
    void getDoctorAvailability_Success() {
        Doctor doctor = new Doctor();
        doctor.setDoctorCode("DOC0001");
        doctor.setAvailableFrom(LocalTime.of(9, 0));
        doctor.setAvailableTo(LocalTime.of(17, 0));
        doctor.setStatus(true);

        when(doctorRepository.findByDoctorCode("DOC0001")).thenReturn(Optional.of(doctor));

        DoctorAvailabilityResponse response = doctorService.getDoctorAvailability("DOC0001");

        assertNotNull(response);
        assertEquals("DOC0001", response.getDoctorCode());
        assertEquals(LocalTime.of(9, 0), response.getAvailableFrom());
    }
}
