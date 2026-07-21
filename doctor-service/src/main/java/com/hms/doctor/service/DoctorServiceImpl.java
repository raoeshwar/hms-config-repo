package com.hms.doctor.service;

import com.hms.doctor.dto.DoctorRequest;
import com.hms.doctor.dto.DoctorResponse;
import com.hms.doctor.dto.AvailabilityRequest;
import com.hms.doctor.dto.DoctorAvailabilityResponse;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.doctor.exception.DuplicateEmailException;
import com.hms.doctor.exception.DuplicatePhoneException;
import com.hms.doctor.exception.ValidationException;
import com.hms.doctor.repository.DoctorRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public DoctorResponse registerDoctor(DoctorRequest request) {
        if (request.getAvailableTo().isBefore(request.getAvailableFrom())) {
            throw new ValidationException("Available To time must be after Available From time");
        }
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        if (doctorRepository.existsByPhone(request.getPhone())) {
            throw new DuplicatePhoneException("Phone number already exists");
        }

        Doctor doctor = mapToEntity(request);
        doctor.setDoctorCode(generateDoctorCode());
        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToResponse(savedDoctor);
    }

    @Override
    @Transactional
    @CachePut(value = "doctor", key = "#doctorCode")
    public DoctorResponse updateDoctor(String doctorCode, DoctorRequest request) {
        if (request.getAvailableTo().isBefore(request.getAvailableFrom())) {
            throw new ValidationException("Available To time must be after Available From time");
        }
        Doctor doctor = doctorRepository.findByDoctorCode(doctorCode)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

        if (!doctor.getEmail().equals(request.getEmail()) && doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        if (!doctor.getPhone().equals(request.getPhone()) && doctorRepository.existsByPhone(request.getPhone())) {
            throw new DuplicatePhoneException("Phone number already exists");
        }

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setGender(request.getGender());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperience(request.getExperience());
        doctor.setHospital(request.getHospital());
        doctor.setDepartment(request.getDepartment());
        doctor.setAvailableFrom(request.getAvailableFrom());
        doctor.setAvailableTo(request.getAvailableTo());
        doctor.setConsultationFee(request.getConsultationFee());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return mapToResponse(updatedDoctor);
    }

    @Override
    @Cacheable(value = "doctor", key = "#doctorCode")
    public DoctorResponse getDoctor(String doctorCode) {
        log.info("Fetching doctor from database: {}", doctorCode);
        Doctor doctor = doctorRepository.findByDoctorCode(doctorCode)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        return mapToResponse(doctor);
    }

    @Override
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> getAvailableDoctors() {
        return doctorRepository.findByStatusTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> searchDoctors(String query) {
        Specification<Doctor> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("status")));

            if (query != null && !query.isEmpty()) {
                String search = "%" + query.toLowerCase() + "%";
                Predicate firstName = cb.like(cb.lower(root.get("firstName")), search);
                Predicate lastName = cb.like(cb.lower(root.get("lastName")), search);
                Predicate specialization = cb.like(cb.lower(root.get("specialization")), search);
                predicates.add(cb.or(firstName, lastName, specialization));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return doctorRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "doctor", key = "#doctorCode")
    public void disableDoctor(String doctorCode) {
        Doctor doctor = doctorRepository.findByDoctorCode(doctorCode)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        doctor.setStatus(false);
        doctorRepository.save(doctor);
    }
    
    @Override
    public DoctorAvailabilityResponse getDoctorAvailability(String doctorCode) {
        Doctor doctor = doctorRepository.findByDoctorCode(doctorCode)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
                
        LocalTime now = LocalTime.now();
        boolean isAvailableNow = doctor.getStatus() 
                && now.isAfter(doctor.getAvailableFrom()) 
                && now.isBefore(doctor.getAvailableTo());
                
        return DoctorAvailabilityResponse.builder()
                .doctorCode(doctor.getDoctorCode())
                .availableFrom(doctor.getAvailableFrom())
                .availableTo(doctor.getAvailableTo())
                .isAvailableNow(isAvailableNow)
                .build();
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "doctor", key = "#doctorCode")
    public DoctorAvailabilityResponse updateDoctorAvailability(String doctorCode, AvailabilityRequest request) {
        if (request.getAvailableTo().isBefore(request.getAvailableFrom())) {
            throw new ValidationException("Available To time must be after Available From time");
        }
        Doctor doctor = doctorRepository.findByDoctorCode(doctorCode)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
                
        doctor.setAvailableFrom(request.getAvailableFrom());
        doctor.setAvailableTo(request.getAvailableTo());
        doctorRepository.save(doctor);
        
        return getDoctorAvailability(doctorCode);
    }

    private String generateDoctorCode() {
        long count = doctorRepository.count() + 1;
        return String.format("DOC%04d", count);
    }

    private Doctor mapToEntity(DoctorRequest request) {
        return Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experience(request.getExperience())
                .hospital(request.getHospital())
                .department(request.getDepartment())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .consultationFee(request.getConsultationFee())
                .status(true)
                .build();
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorCode(doctor.getDoctorCode())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .gender(doctor.getGender())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experience(doctor.getExperience())
                .hospital(doctor.getHospital())
                .department(doctor.getDepartment())
                .availableFrom(doctor.getAvailableFrom())
                .availableTo(doctor.getAvailableTo())
                .consultationFee(doctor.getConsultationFee())
                .status(doctor.getStatus())
                .build();
    }
}
