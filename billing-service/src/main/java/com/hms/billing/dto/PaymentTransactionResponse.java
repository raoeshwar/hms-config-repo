package com.hms.billing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentTransactionResponse {
    private String billNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String referenceNumber;
    private LocalDateTime transactionDate;
}
