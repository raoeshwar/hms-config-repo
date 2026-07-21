package com.hms.patient.service;

import com.hms.patient.dto.PatientRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.DuplicateEmailException;
import com.hms.patient.exception.DuplicatePhoneException;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.repository.PatientRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public PatientResponse addPatient(PatientRequest request) {
        log.info("Adding new patient: {}", request.getEmail());
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        if (patientRepository.existsByPhone(request.getPhone())) {
            throw new DuplicatePhoneException("Phone number already exists");
        }

        Patient patient = mapToEntity(request);
        patient.setPatientCode(generatePatientCode());
        patient.setAge(calculateAge(request.getDateOfBirth()));

        Patient savedPatient = patientRepository.save(patient);
        return mapToResponse(savedPatient);
    }

    @Override
    @Transactional
    @CachePut(value = "patients", key = "#patientCode")
    public PatientResponse updatePatient(String patientCode, PatientRequest request) {
        log.info("Updating patient: {}", patientCode);
        Patient patient = patientRepository.findByPatientCodeAndStatusTrue(patientCode)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        if (!patient.getEmail().equals(request.getEmail()) && patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        if (!patient.getPhone().equals(request.getPhone()) && patientRepository.existsByPhone(request.getPhone())) {
            throw new DuplicatePhoneException("Phone number already exists");
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAge(calculateAge(request.getDateOfBirth()));
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setCountry(request.getCountry());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactNumber(request.getEmergencyContactNumber());

        Patient updatedPatient = patientRepository.save(patient);
        return mapToResponse(updatedPatient);
    }

    @Override
    @Cacheable(value = "patients", key = "#patientCode")
    public PatientResponse getPatientById(String patientCode) {
        Patient patient = patientRepository.findByPatientCodeAndStatusTrue(patientCode)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        return mapToResponse(patient);
    }

    @Override
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public List<PatientResponse> searchPatients(String name, String phone, String email, String bloodGroup, String city) {
        Specification<Patient> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("status")));

            if (name != null && !name.isEmpty()) {
                Predicate firstName = cb.like(cb.lower(root.get("firstName")), "%" + name.toLowerCase() + "%");
                Predicate lastName = cb.like(cb.lower(root.get("lastName")), "%" + name.toLowerCase() + "%");
                predicates.add(cb.or(firstName, lastName));
            }
            if (phone != null && !phone.isEmpty()) {
                predicates.add(cb.equal(root.get("phone"), phone));
            }
            if (email != null && !email.isEmpty()) {
                predicates.add(cb.equal(root.get("email"), email));
            }
            if (bloodGroup != null && !bloodGroup.isEmpty()) {
                predicates.add(cb.equal(root.get("bloodGroup"), bloodGroup));
            }
            if (city != null && !city.isEmpty()) {
                predicates.add(cb.equal(root.get("city"), city));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return patientRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "patients", key = "#patientCode")
    public void deletePatient(String patientCode) {
        log.info("Soft deleting patient: {}", patientCode);
        Patient patient = patientRepository.findByPatientCodeAndStatusTrue(patientCode)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        patient.setStatus(false);
        patientRepository.save(patient);
    }

    @Override
    public PatientResponse getPatientProfile(String email) {
        Patient patient = patientRepository.findByEmailAndStatusTrue(email)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for email: " + email));
        return mapToResponse(patient);
    }

    private String generatePatientCode() {
        long count = patientRepository.count() + 1;
        return String.format("PAT%06d", count);
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private Patient mapToEntity(PatientRequest request) {
        return Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactNumber(request.getEmergencyContactNumber())
                .status(true)
                .build();
    }

    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientCode())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth())
                .age(patient.getAge())
                .bloodGroup(patient.getBloodGroup())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .city(patient.getCity())
                .state(patient.getState())
                .country(patient.getCountry())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactNumber(patient.getEmergencyContactNumber())
                .build();
    }
}
