package com.hms.doctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class DoctorRequest {

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    private String gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone Number must be valid 10 digits")
    private String phone;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    private String qualification;

    @NotNull(message = "Experience is required")
    private Integer experience;

    private String hospital;
    private String department;

    private LocalTime availableFrom;
    private LocalTime availableTo;

    private BigDecimal consultationFee;
}
