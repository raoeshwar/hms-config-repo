package com.hms.appointment.client;

import com.hms.appointment.dto.ApiResponse;
import com.hms.appointment.dto.PatientClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-service", url = "${patient.service.url:http://localhost:8081}")
public interface PatientClient {

    @GetMapping("/api/v1/patients/{patientId}")
    ApiResponse<PatientClientResponse> getPatientById(@PathVariable("patientId") Long patientId);
}
