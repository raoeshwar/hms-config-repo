package com.hms.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    private String gender;

    @Past(message = "Date of Birth cannot be a future date")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-)$", message = "Invalid blood group")
    private String bloodGroup;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone Number must be valid 10 digits")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format is required")
    private String email;

    private String address;
    private String city;
    private String state;
    private String country;

    private String emergencyContactName;

    @Pattern(regexp = "^(\\d{10})?$", message = "Emergency contact must be valid 10 digits if provided")
    private String emergencyContactNumber;
}
