package com.hospital.appointment.repository;

import com.hospital.appointment.domain.entity.Appointment;
import com.hospital.appointment.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findByMedicoId(UUID medicoId, Pageable pageable);

    Page<Appointment> findByPacienteId(UUID pacienteId, Pageable pageable);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @Query("""
            SELECT a FROM Appointment a
            WHERE (:medicoId IS NULL OR a.medico.id = :medicoId)
              AND (:pacienteId IS NULL OR a.paciente.id = :pacienteId)
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<Appointment> findWithFilters(
            @Param("medicoId") UUID medicoId,
            @Param("pacienteId") UUID pacienteId,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    @Query("SELECT a FROM Appointment a WHERE a.paciente.user.id = :userId")
    Page<Appointment> findByPacienteUserId(@Param("userId") UUID userId, Pageable pageable);
}
