package com.hms.billing.service;

import com.hms.billing.dto.BillRequest;
import com.hms.billing.dto.BillResponse;
import com.hms.billing.dto.PaymentRequest;
import com.hms.billing.dto.PaymentTransactionResponse;

import java.util.List;

public interface BillingService {
    BillResponse generateBill(BillRequest request);
    BillResponse getBillById(String billNumber);
    List<BillResponse> getBillsByPatient(Long patientId);
    PaymentTransactionResponse recordPayment(String billNumber, PaymentRequest request);
    List<PaymentTransactionResponse> getPaymentHistory(String billNumber);
}
