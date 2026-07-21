package com.hms.doctor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.doctor.dto.AvailabilityRequest;
import com.hms.doctor.dto.DoctorAvailabilityResponse;
import com.hms.doctor.dto.DoctorRequest;
import com.hms.doctor.dto.DoctorResponse;
import com.hms.doctor.security.JwtAuthFilter;
import com.hms.doctor.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testRegisterDoctor() throws Exception {
        DoctorRequest request = new DoctorRequest();
        request.setFirstName("Ramesh");
        request.setLastName("Kumar");
        request.setEmail("ramesh@gmail.com");
        request.setPhone("9876543210");
        request.setSpecialization("Cardiology");
        request.setExperience(10);
        request.setHospital("City Hospital");
        request.setAvailableFrom(LocalTime.of(9, 0));
        request.setAvailableTo(LocalTime.of(17, 0));
        request.setConsultationFee(BigDecimal.valueOf(800));

        DoctorResponse response = DoctorResponse.builder()
                .doctorCode("DOC0001")
                .firstName("Ramesh")
                .build();

        when(doctorService.registerDoctor(any(DoctorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/doctors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.doctorCode").value("DOC0001"));
    }

    @Test
    void testGetDoctorAvailability() throws Exception {
        DoctorAvailabilityResponse response = DoctorAvailabilityResponse.builder()
                .doctorCode("DOC0001")
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(17, 0))
                .isAvailableNow(true)
                .build();

        when(doctorService.getDoctorAvailability(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/availability/DOC0001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isAvailableNow").value(true));
    }

    @Test
    void testUpdateDoctorAvailability() throws Exception {
        AvailabilityRequest request = new AvailabilityRequest();
        request.setAvailableFrom(LocalTime.of(10, 0));
        request.setAvailableTo(LocalTime.of(18, 0));

        DoctorAvailabilityResponse response = DoctorAvailabilityResponse.builder()
                .doctorCode("DOC0001")
                .availableFrom(LocalTime.of(10, 0))
                .availableTo(LocalTime.of(18, 0))
                .isAvailableNow(true)
                .build();

        when(doctorService.updateDoctorAvailability(anyString(), any(AvailabilityRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/doctors/DOC0001/availability")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableFrom").value("10:00:00"));
    }
}
