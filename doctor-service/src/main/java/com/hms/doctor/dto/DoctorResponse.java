package com.hms.doctor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponse implements Serializable {
    private String doctorCode;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phone;
    private String specialization;
    private String qualification;
    private Integer experience;
    private String hospital;
    private String department;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private BigDecimal consultationFee;
    private Boolean status;
}
