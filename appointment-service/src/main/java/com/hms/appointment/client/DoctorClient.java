package com.hms.appointment.client;

import com.hms.appointment.dto.ApiResponse;
import com.hms.appointment.dto.DoctorClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service", url = "${doctor.service.url:http://localhost:8082}")
public interface DoctorClient {

    @GetMapping("/api/v1/doctors/availability/{doctorId}")
    ApiResponse<DoctorClientResponse> getDoctorAvailability(@PathVariable("doctorId") Long doctorId);
}
