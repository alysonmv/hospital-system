package com.hospital.history.messaging;

import com.hospital.history.domain.entity.Appointment;
import com.hospital.history.domain.entity.Patient;
import com.hospital.history.domain.entity.User;
import com.hospital.history.domain.enums.AppointmentStatus;
import com.hospital.history.messaging.dto.AppointmentEvent;
import com.hospital.history.repository.AppointmentRepository;
import com.hospital.history.repository.PatientRepository;
import com.hospital.history.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSyncService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public void syncAppointment(AppointmentEvent event) {
        log.info("Syncing appointment [{}] event [{}]", event.getAppointmentId(), event.getEventType());

        switch (event.getEventType()) {
            case "CREATED" -> createAppointment(event);
            case "UPDATED" -> updateAppointment(event);
            case "CANCELLED" -> cancelAppointment(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void createAppointment(AppointmentEvent event) {
        // Se já existe (idempotência), ignora
        if (appointmentRepository.existsById(event.getAppointmentId())) {
            log.info("Appointment [{}] already exists, skipping", event.getAppointmentId());
            return;
        }

        User medico = getOrCreateUser(event.getMedicoId(), event.getMedicoNome(), event.getMedicoEmail(), "ROLE_MEDICO");
        Patient paciente = getOrCreatePatient(event.getPacienteId(), event.getPacienteNome(), event.getPacienteTelefone());

        Appointment appointment = Appointment.builder()
                .id(event.getAppointmentId())
                .dataConsulta(event.getDataConsulta())
                .status(AppointmentStatus.valueOf(event.getStatus()))
                .descricao(event.getDescricao())
                .medico(medico)
                .paciente(paciente)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        appointmentRepository.save(appointment);
        log.info("Appointment [{}] created in history", event.getAppointmentId());
    }

    private void updateAppointment(AppointmentEvent event) {
        appointmentRepository.findById(event.getAppointmentId()).ifPresentOrElse(
            appointment -> {
                appointment.setDataConsulta(event.getDataConsulta());
                appointment.setStatus(AppointmentStatus.valueOf(event.getStatus()));
                appointment.setDescricao(event.getDescricao());
                appointment.setUpdatedAt(LocalDateTime.now());
                appointmentRepository.save(appointment);
                log.info("Appointment [{}] updated in history", event.getAppointmentId());
            },
            () -> {
                log.warn("Appointment [{}] not found for update, creating instead", event.getAppointmentId());
                createAppointment(event);
            }
        );
    }

    private void cancelAppointment(AppointmentEvent event) {
        appointmentRepository.findById(event.getAppointmentId()).ifPresent(appointment -> {
            appointment.setStatus(AppointmentStatus.CANCELADA);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);
            log.info("Appointment [{}] cancelled in history", event.getAppointmentId());
        });
    }

    private User getOrCreateUser(java.util.UUID id, String nome, String email, String role) {
        return userRepository.findById(id).orElseGet(() -> {
            User user = User.builder()
                    .id(id)
                    .nome(nome)
                    .email(email)
                    .senha("N/A")
                    .role(role)
                    .ativo(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return userRepository.save(user);
        });
    }

    private Patient getOrCreatePatient(java.util.UUID id, String nome, String telefone) {
        return patientRepository.findById(id).orElseGet(() -> {
            Patient patient = Patient.builder()
                    .id(id)
                    .nome(nome)
                    .telefone(telefone)
                    .dataNascimento(java.time.LocalDate.of(1900, 1, 1))
                    .cpf("000.000.000-00")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return patientRepository.save(patient);
        });
    }
}
