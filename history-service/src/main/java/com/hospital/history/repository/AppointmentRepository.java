package com.hospital.history.repository;

import com.hospital.history.domain.entity.Appointment;
import com.hospital.history.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a WHERE a.paciente.id = :pacienteId AND a.dataConsulta >= :now ORDER BY a.dataConsulta ASC")
    List<Appointment> findFutureByPacienteId(@Param("pacienteId") UUID pacienteId,
                                              @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a WHERE a.paciente.id = :pacienteId AND a.dataConsulta < :now ORDER BY a.dataConsulta DESC")
    List<Appointment> findPastByPacienteId(@Param("pacienteId") UUID pacienteId,
                                            @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a WHERE a.dataConsulta >= :now ORDER BY a.dataConsulta ASC")
    List<Appointment> findAllFuture(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a WHERE a.dataConsulta < :now ORDER BY a.dataConsulta DESC")
    List<Appointment> findAllPast(@Param("now") LocalDateTime now);
}
