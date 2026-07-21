package com.hms.billing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillResponse {
    private String billNumber;
    private Long patientId;
    private Long appointmentId;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private String paymentStatus;
}
