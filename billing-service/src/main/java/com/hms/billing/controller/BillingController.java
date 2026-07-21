package com.hms.billing.controller;

import com.hms.billing.dto.ApiResponse;
import com.hms.billing.dto.BillRequest;
import com.hms.billing.dto.BillResponse;
import com.hms.billing.dto.PaymentRequest;
import com.hms.billing.dto.PaymentTransactionResponse;
import com.hms.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillResponse>> generateBill(@Valid @RequestBody BillRequest request) {
        BillResponse response = billingService.generateBill(request);
        return ResponseEntity.ok(ApiResponse.success("Bill generated successfully.", response));
    }

    @GetMapping("/{billNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable String billNumber) {
        BillResponse response = billingService.getBillById(billNumber);
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved successfully.", response));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBillsByPatient(@PathVariable Long patientId) {
        List<BillResponse> response = billingService.getBillsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient bills retrieved successfully.", response));
    }

    @PutMapping("/{billNumber}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> recordPayment(
            @PathVariable String billNumber, 
            @Valid @RequestBody PaymentRequest request) {
        PaymentTransactionResponse response = billingService.recordPayment(billNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully.", response));
    }

    @GetMapping("/{billNumber}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> getPaymentHistory(@PathVariable String billNumber) {
        List<PaymentTransactionResponse> response = billingService.getPaymentHistory(billNumber);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully.", response));
    }
}
