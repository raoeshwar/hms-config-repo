package com.hms.doctor.repository;

import com.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByDoctorCode(String doctorCode);
    List<Doctor> findByStatusTrue();
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
