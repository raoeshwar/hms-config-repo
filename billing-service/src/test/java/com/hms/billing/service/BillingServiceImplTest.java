package com.hms.billing.service;

import com.hms.billing.client.AppointmentClient;
import com.hms.billing.client.AppointmentClientResponse;
import com.hms.billing.dto.ApiResponse;
import com.hms.billing.dto.BillRequest;
import com.hms.billing.dto.BillResponse;
import com.hms.billing.entity.Bill;
import com.hms.billing.entity.PaymentStatus;
import com.hms.billing.exception.AppointmentNotCompletedException;
import com.hms.billing.exception.InsuranceValidationException;
import com.hms.billing.kafka.BillingEventProducer;
import com.hms.billing.repository.BillRepository;
import com.hms.billing.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillingServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private PaymentTransactionRepository paymentRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private BillingEventProducer eventProducer;

    @InjectMocks
    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateBill_Success() {
        BillRequest request = new BillRequest();
        request.setPatientId(101L);
        request.setAppointmentId(5001L);
        request.setMedicalRecordId(7001L);
        request.setConsultationFee(new BigDecimal("800"));
        request.setLabCharges(new BigDecimal("1200"));
        request.setMedicineCharges(new BigDecimal("650"));
        request.setOtherCharges(new BigDecimal("150"));
        request.setInsuranceAmount(new BigDecimal("1000"));

        AppointmentClientResponse appointmentResponse = new AppointmentClientResponse();
        appointmentResponse.setPatientId(101L);
        appointmentResponse.setStatus("COMPLETED");

        when(billRepository.existsByAppointmentId(5001L)).thenReturn(false);
        when(appointmentClient.getAppointmentById("5001"))
                .thenReturn(ApiResponse.success("Success", appointmentResponse));

        Bill savedBill = Bill.builder()
                .billNumber("BILL000001")
                .patientId(101L)
                .appointmentId(5001L)
                .totalAmount(new BigDecimal("2800"))
                .insuranceAmount(new BigDecimal("1000"))
                .balanceAmount(new BigDecimal("1800"))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);
        when(billRepository.count()).thenReturn(0L);

        BillResponse response = billingService.generateBill(request);

        assertNotNull(response);
        assertEquals("BILL000001", response.getBillNumber());
        assertEquals(new BigDecimal("2800"), response.getTotalAmount());
        assertEquals(new BigDecimal("1800"), response.getBalanceAmount());
        verify(eventProducer, times(1)).publishBillingEvent(eq("bill-generated"), anyString(), anyString(), any());
    }

    @Test
    void generateBill_InsuranceExceedsTotal_ThrowsException() {
        BillRequest request = new BillRequest();
        request.setPatientId(101L);
        request.setAppointmentId(5001L);
        request.setConsultationFee(new BigDecimal("100"));
        request.setLabCharges(new BigDecimal("100"));
        request.setMedicineCharges(new BigDecimal("0"));
        request.setOtherCharges(new BigDecimal("0"));
        request.setInsuranceAmount(new BigDecimal("500"));

        AppointmentClientResponse appointmentResponse = new AppointmentClientResponse();
        appointmentResponse.setPatientId(101L);
        appointmentResponse.setStatus("COMPLETED");

        when(billRepository.existsByAppointmentId(5001L)).thenReturn(false);
        when(appointmentClient.getAppointmentById("5001"))
                .thenReturn(ApiResponse.success("Success", appointmentResponse));

        assertThrows(InsuranceValidationException.class, () -> billingService.generateBill(request));
    }
}
