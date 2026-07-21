package com.hms.billing.service;

import com.hms.billing.client.AppointmentClient;
import com.hms.billing.client.AppointmentClientResponse;
import com.hms.billing.dto.ApiResponse;
import com.hms.billing.dto.BillRequest;
import com.hms.billing.dto.BillResponse;
import com.hms.billing.dto.PaymentRequest;
import com.hms.billing.dto.PaymentTransactionResponse;
import com.hms.billing.entity.Bill;
import com.hms.billing.entity.PaymentStatus;
import com.hms.billing.entity.PaymentTransaction;
import com.hms.billing.exception.AppointmentNotCompletedException;
import com.hms.billing.exception.BillNotFoundException;
import com.hms.billing.exception.InsuranceValidationException;
import com.hms.billing.exception.InvalidPaymentException;
import com.hms.billing.exception.ValidationException;
import com.hms.billing.kafka.BillingEventProducer;
import com.hms.billing.repository.BillRepository;
import com.hms.billing.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final AppointmentClient appointmentClient;
    private final BillingEventProducer eventProducer;

    @Override
    @Transactional
    public BillResponse generateBill(BillRequest request) {
        if (billRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new ValidationException("Bill already generated for this appointment");
        }

        try {
            ApiResponse<AppointmentClientResponse> response = appointmentClient.getAppointmentById(String.valueOf(request.getAppointmentId()));
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new ValidationException("Appointment not found");
            }
            AppointmentClientResponse appointmentData = response.getData();

            if (!"COMPLETED".equalsIgnoreCase(appointmentData.getStatus())) {
                throw new AppointmentNotCompletedException("Bills can only be generated for COMPLETED appointments");
            }
            
            if (!appointmentData.getPatientId().equals(request.getPatientId())) {
                throw new ValidationException("Appointment does not match the provided Patient ID");
            }
        } catch (AppointmentNotCompletedException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to validate appointment: " + e.getMessage());
        }

        BigDecimal totalAmount = request.getConsultationFee()
                .add(request.getLabCharges())
                .add(request.getMedicineCharges())
                .add(request.getOtherCharges());

        if (request.getInsuranceAmount().compareTo(totalAmount) > 0) {
            throw new InsuranceValidationException("Insurance coverage cannot exceed the total bill amount");
        }

        BigDecimal balanceAmount = totalAmount.subtract(request.getInsuranceAmount());
        PaymentStatus initialStatus = balanceAmount.compareTo(BigDecimal.ZERO) == 0 ? PaymentStatus.PAID : PaymentStatus.PENDING;

        Bill bill = Bill.builder()
                .billNumber(generateBillNumber())
                .patientId(request.getPatientId())
                .appointmentId(request.getAppointmentId())
                .medicalRecordId(request.getMedicalRecordId())
                .consultationFee(request.getConsultationFee())
                .labCharges(request.getLabCharges())
                .medicineCharges(request.getMedicineCharges())
                .otherCharges(request.getOtherCharges())
                .totalAmount(totalAmount)
                .insuranceAmount(request.getInsuranceAmount())
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(balanceAmount)
                .paymentStatus(initialStatus)
                .build();

        Bill savedBill = billRepository.save(bill);

        eventProducer.publishBillingEvent("bill-generated", savedBill.getBillNumber(), "BILL_GENERATED", 
                new BillingEventProducer.BillGeneratedEvent(savedBill.getPatientId(), savedBill.getTotalAmount(), savedBill.getBalanceAmount()));

        return mapToResponse(savedBill);
    }

    @Override
    public BillResponse getBillById(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new BillNotFoundException("Bill not found"));
        return mapToResponse(bill);
    }

    @Override
    public List<BillResponse> getBillsByPatient(Long patientId) {
        return billRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentTransactionResponse recordPayment(String billNumber, PaymentRequest request) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new BillNotFoundException("Bill not found"));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidPaymentException("Bill is already fully paid");
        }

        if (request.getPaidAmount().compareTo(bill.getBalanceAmount()) > 0) {
            throw new InvalidPaymentException("Paid Amount cannot exceed Balance Amount");
        }

        bill.setPaidAmount(bill.getPaidAmount().add(request.getPaidAmount()));
        bill.setBalanceAmount(bill.getBalanceAmount().subtract(request.getPaidAmount()));
        bill.setPaymentMethod(request.getPaymentMethod());

        if (bill.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
            bill.setPaymentStatus(PaymentStatus.PAID);
        } else {
            bill.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }

        Bill savedBill = billRepository.save(bill);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .bill(savedBill)
                .amount(request.getPaidAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PAID)
                .referenceNumber(UUID.randomUUID().toString())
                .build();

        PaymentTransaction savedTransaction = paymentRepository.save(transaction);

        String topic = savedBill.getPaymentStatus() == PaymentStatus.PAID ? "payment-success" : "payment-partial";
        eventProducer.publishBillingEvent(topic, savedBill.getBillNumber(), "PAYMENT_COMPLETED", 
                new BillingEventProducer.PaymentEvent(savedBill.getPatientId(), request.getPaidAmount(), savedBill.getPaymentStatus().name()));

        return mapToTransactionResponse(savedTransaction);
    }

    @Override
    public List<PaymentTransactionResponse> getPaymentHistory(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new BillNotFoundException("Bill not found"));
                
        return paymentRepository.findByBillId(bill.getId()).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private String generateBillNumber() {
        long count = billRepository.count() + 1;
        return String.format("BILL%06d", count);
    }

    private BillResponse mapToResponse(Bill bill) {
        return BillResponse.builder()
                .billNumber(bill.getBillNumber())
                .patientId(bill.getPatientId())
                .appointmentId(bill.getAppointmentId())
                .totalAmount(bill.getTotalAmount())
                .insuranceAmount(bill.getInsuranceAmount())
                .paidAmount(bill.getPaidAmount())
                .balanceAmount(bill.getBalanceAmount())
                .paymentStatus(bill.getPaymentStatus().name())
                .build();
    }
    
    private PaymentTransactionResponse mapToTransactionResponse(PaymentTransaction transaction) {
        return PaymentTransactionResponse.builder()
                .billNumber(transaction.getBill().getBillNumber())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentStatus(transaction.getPaymentStatus().name())
                .referenceNumber(transaction.getReferenceNumber())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }
}
