package com.hms.billing.client;

import com.hms.billing.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "appointment-service", url = "${appointment.service.url:http://localhost:8083}")
public interface AppointmentClient {

    @GetMapping("/api/v1/appointments/{appointmentId}")
    ApiResponse<AppointmentClientResponse> getAppointmentById(@PathVariable("appointmentId") String appointmentId);
}
