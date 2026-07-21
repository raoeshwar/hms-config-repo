package com.hms.medicalrecord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.medicalrecord.dto.MedicalRecordRequest;
import com.hms.medicalrecord.dto.MedicalRecordResponse;
import com.hms.medicalrecord.security.JwtAuthFilter;
import com.hms.medicalrecord.service.MedicalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateMedicalRecord() throws Exception {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setPatientId(101L);
        request.setDoctorId(25L);
        request.setAppointmentId(5001L);
        request.setDiagnosis("Viral Fever");
        request.setPrescription("Paracetamol");

        MedicalRecordResponse response = MedicalRecordResponse.builder()
                .recordNumber("MR000001")
                .diagnosis("Viral Fever")
                .build();

        when(medicalRecordService.createMedicalRecord(any(MedicalRecordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/medical-records")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recordNumber").value("MR000001"));
    }
}
