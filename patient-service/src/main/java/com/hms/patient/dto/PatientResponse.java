package com.hms.patient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String patientId;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private Integer age;
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String country;
    private String emergencyContactName;
    private String emergencyContactNumber;
}
