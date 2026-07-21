package com.hms.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.billing.dto.BillRequest;
import com.hms.billing.dto.BillResponse;
import com.hms.billing.security.JwtAuthFilter;
import com.hms.billing.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGenerateBill() throws Exception {
        BillRequest request = new BillRequest();
        request.setPatientId(101L);
        request.setAppointmentId(5001L);
        request.setMedicalRecordId(7001L);
        request.setConsultationFee(new BigDecimal("800"));
        request.setLabCharges(new BigDecimal("1200"));
        request.setMedicineCharges(new BigDecimal("650"));
        request.setOtherCharges(new BigDecimal("150"));
        request.setInsuranceAmount(new BigDecimal("1000"));

        BillResponse response = BillResponse.builder()
                .billNumber("BILL000001")
                .totalAmount(new BigDecimal("2800"))
                .balanceAmount(new BigDecimal("1800"))
                .build();

        when(billingService.generateBill(any(BillRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/bills")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.billNumber").value("BILL000001"));
    }
}
