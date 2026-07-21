package com.hms.medicalrecord.repository;

import com.hms.medicalrecord.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByRecordNumber(String recordNumber);
    List<MedicalRecord> findByPatientId(Long patientId);
    boolean existsByAppointmentId(Long appointmentId);
}
