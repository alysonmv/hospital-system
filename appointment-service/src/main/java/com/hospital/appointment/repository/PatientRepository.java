package com.hospital.appointment.repository;

import com.hospital.appointment.domain.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByCpf(String cpf);

    Optional<Patient> findByUserId(UUID userId);

    boolean existsByCpf(String cpf);
}
