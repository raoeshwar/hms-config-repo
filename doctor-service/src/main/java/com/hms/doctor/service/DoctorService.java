package com.hms.doctor.service;

import com.hms.doctor.dto.DoctorRequest;
import com.hms.doctor.dto.DoctorResponse;

import java.util.List;

public interface DoctorService {
    DoctorResponse registerDoctor(DoctorRequest request);
    DoctorResponse updateDoctor(String doctorCode, DoctorRequest request);
    DoctorResponse getDoctor(String doctorCode);
    List<DoctorResponse> getAllDoctors();
    List<DoctorResponse> getAvailableDoctors();
    List<DoctorResponse> searchDoctors(String query);
    void disableDoctor(String doctorCode);
    
    com.hms.doctor.dto.DoctorAvailabilityResponse getDoctorAvailability(String doctorCode);
    com.hms.doctor.dto.DoctorAvailabilityResponse updateDoctorAvailability(String doctorCode, com.hms.doctor.dto.AvailabilityRequest request);
}
