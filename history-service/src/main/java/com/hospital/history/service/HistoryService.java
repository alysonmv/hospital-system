package com.hospital.history.service;

import com.hospital.history.domain.entity.Appointment;
import com.hospital.history.domain.entity.MedicalHistory;
import com.hospital.history.domain.entity.Patient;
import com.hospital.history.dto.*;
import com.hospital.history.exception.ResourceNotFoundException;
import com.hospital.history.repository.AppointmentRepository;
import com.hospital.history.repository.MedicalHistoryRepository;
import com.hospital.history.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HistoryService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AppointmentRepository appointmentRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;

    public List<MedicalHistoryDTO> buscarHistoricoPaciente(UUID pacienteId) {
        log.debug("Fetching medical history for patient: {}", pacienteId);
        validatePatientExists(pacienteId);
        return medicalHistoryRepository
                .findByPacienteIdOrderByDataRegistroDesc(pacienteId)
                .stream()
                .map(this::toMedicalHistoryDTO)
                .toList();
    }

    public List<AppointmentDTO> listarConsultasFuturas(UUID pacienteId) {
        log.debug("Fetching future appointments, pacienteId={}", pacienteId);
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = pacienteId != null
                ? appointmentRepository.findFutureByPacienteId(pacienteId, now)
                : appointmentRepository.findAllFuture(now);
        return appointments.stream().map(this::toAppointmentDTO).toList();
    }

    public List<AppointmentDTO> listarConsultasPassadas(UUID pacienteId) {
        log.debug("Fetching past appointments, pacienteId={}", pacienteId);
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = pacienteId != null
                ? appointmentRepository.findPastByPacienteId(pacienteId, now)
                : appointmentRepository.findAllPast(now);
        return appointments.stream().map(this::toAppointmentDTO).toList();
    }

    public AppointmentDTO buscarConsultaPorId(UUID id) {
        log.debug("Fetching appointment by id: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Consulta", id));
        return toAppointmentDTO(appointment);
    }

    private void validatePatientExists(UUID pacienteId) {
        if (!patientRepository.existsById(pacienteId)) {
            throw ResourceNotFoundException.of("Paciente", pacienteId);
        }
    }

    private AppointmentDTO toAppointmentDTO(Appointment a) {
        return AppointmentDTO.builder()
                .id(a.getId())
                .dataConsulta(a.getDataConsulta() != null ? a.getDataConsulta().format(DT_FMT) : null)
                .status(a.getStatus().name())
                .descricao(a.getDescricao())
                .medico(a.getMedico() != null ? UserDTO.builder()
                        .id(a.getMedico().getId())
                        .nome(a.getMedico().getNome())
                        .email(a.getMedico().getEmail())
                        .role(a.getMedico().getRole())
                        .build() : null)
                .paciente(a.getPaciente() != null ? toPatientDTO(a.getPaciente()) : null)
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().format(DT_FMT) : null)
                .updatedAt(a.getUpdatedAt() != null ? a.getUpdatedAt().format(DT_FMT) : null)
                .build();
    }

    private MedicalHistoryDTO toMedicalHistoryDTO(MedicalHistory h) {
        return MedicalHistoryDTO.builder()
                .id(h.getId())
                .descricao(h.getDescricao())
                .dataRegistro(h.getDataRegistro() != null ? h.getDataRegistro().format(DT_FMT) : null)
                .paciente(h.getPaciente() != null ? toPatientDTO(h.getPaciente()) : null)
                .build();
    }

    private PatientDTO toPatientDTO(Patient p) {
        return PatientDTO.builder()
                .id(p.getId())
                .nome(p.getNome())
                .telefone(p.getTelefone())
                .dataNascimento(p.getDataNascimento() != null ? p.getDataNascimento().format(DATE_FMT) : null)
                .build();
    }
}
