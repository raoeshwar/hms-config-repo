package com.hms.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "Paid Amount is required")
    @Min(value = 1, message = "Paid Amount must be greater than 0")
    private BigDecimal paidAmount;

    @NotBlank(message = "Payment Method is required")
    private String paymentMethod;
}
