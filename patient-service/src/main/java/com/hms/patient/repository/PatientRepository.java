package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    
    Optional<Patient> findByPatientCodeAndStatusTrue(String patientCode);
    Optional<Patient> findByEmailAndStatusTrue(String email);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
