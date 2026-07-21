package com.hms.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Medical Record ID is required")
    private Long medicalRecordId;

    @NotNull(message = "Consultation Fee is required")
    @Min(value = 0, message = "Consultation Fee must be greater than or equal to 0")
    private BigDecimal consultationFee;

    @NotNull(message = "Lab Charges is required")
    @Min(value = 0, message = "Lab Charges must be greater than or equal to 0")
    private BigDecimal labCharges;

    @NotNull(message = "Medicine Charges is required")
    @Min(value = 0, message = "Medicine Charges must be greater than or equal to 0")
    private BigDecimal medicineCharges;

    @NotNull(message = "Other Charges is required")
    @Min(value = 0, message = "Other Charges must be greater than or equal to 0")
    private BigDecimal otherCharges;

    @NotNull(message = "Insurance Amount is required")
    @Min(value = 0, message = "Insurance Amount must be greater than or equal to 0")
    private BigDecimal insuranceAmount;
}
