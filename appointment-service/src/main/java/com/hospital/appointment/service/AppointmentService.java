package com.hospital.appointment.service;

import com.hospital.appointment.domain.entity.Appointment;
import com.hospital.appointment.domain.entity.Patient;
import com.hospital.appointment.domain.entity.User;
import com.hospital.appointment.domain.enums.AppointmentStatus;
import com.hospital.appointment.domain.enums.Role;
import com.hospital.appointment.dto.event.AppointmentEvent;
import com.hospital.appointment.dto.request.CreateAppointmentRequest;
import com.hospital.appointment.dto.request.UpdateAppointmentRequest;
import com.hospital.appointment.dto.response.AppointmentResponse;
import com.hospital.appointment.exception.BusinessException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.messaging.AppointmentEventProducer;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentEventProducer eventProducer;

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        User medico = userRepository.findById(request.medicoId())
                .filter(u -> u.getRole() == Role.ROLE_MEDICO)
                .orElseThrow(() -> new BusinessException("Médico não encontrado ou inválido"));

        Patient paciente = patientRepository.findById(request.pacienteId())
                .orElseThrow(() -> ResourceNotFoundException.of("Paciente", request.pacienteId()));

        Appointment appointment = Appointment.builder()
                .dataConsulta(request.dataConsulta())
                .descricao(request.descricao())
                .medico(medico)
                .paciente(paciente)
                .status(AppointmentStatus.AGENDADA)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment created: {}", appointment.getId());

        AppointmentEvent event = AppointmentEvent.of(
                "CREATED", appointment.getId(), appointment.getDataConsulta(),
                appointment.getStatus(), appointment.getDescricao(),
                medico.getId(), medico.getNome(), medico.getEmail(),
                paciente.getId(), paciente.getNome(), paciente.getTelefone()
        );
        eventProducer.publishCreated(event);

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse update(UUID id, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointmentById(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELADA) {
            throw new BusinessException("Não é possível editar uma consulta cancelada");
        }

        if (request.dataConsulta() != null) {
            appointment.setDataConsulta(request.dataConsulta());
        }
        if (request.status() != null) {
            appointment.setStatus(request.status());
        }
        if (request.descricao() != null) {
            appointment.setDescricao(request.descricao());
        }

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment updated: {}", id);

        AppointmentEvent event = AppointmentEvent.of(
                "UPDATED", appointment.getId(), appointment.getDataConsulta(),
                appointment.getStatus(), appointment.getDescricao(),
                appointment.getMedico().getId(), appointment.getMedico().getNome(), appointment.getMedico().getEmail(),
                appointment.getPaciente().getId(), appointment.getPaciente().getNome(), appointment.getPaciente().getTelefone()
        );
        eventProducer.publishUpdated(event);

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public void cancel(UUID id) {
        Appointment appointment = findAppointmentById(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELADA) {
            throw new BusinessException("Consulta já está cancelada");
        }

        appointment.setStatus(AppointmentStatus.CANCELADA);
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled: {}", id);

        AppointmentEvent event = AppointmentEvent.of(
                "CANCELLED", appointment.getId(), appointment.getDataConsulta(),
                appointment.getStatus(), appointment.getDescricao(),
                appointment.getMedico().getId(), appointment.getMedico().getNome(), appointment.getMedico().getEmail(),
                appointment.getPaciente().getId(), appointment.getPaciente().getNome(), appointment.getPaciente().getTelefone()
        );
        eventProducer.publishCancelled(event);
    }

    public AppointmentResponse findById(UUID id) {
        Appointment appointment = findAppointmentById(id);
        checkPatientAccess(appointment);
        return appointmentMapper.toResponse(appointment);
    }

    public Page<AppointmentResponse> findAll(UUID medicoId, UUID pacienteId, AppointmentStatus status, Pageable pageable) {
        User currentUser = getCurrentUser();

        // Paciente só vê as próprias consultas
        if (currentUser.getRole() == Role.ROLE_PACIENTE) {
            return appointmentRepository
                    .findByPacienteUserId(currentUser.getId(), pageable)
                    .map(appointmentMapper::toResponse);
        }

        return appointmentRepository
                .findWithFilters(medicoId, pacienteId, status, pageable)
                .map(appointmentMapper::toResponse);
    }

    private Appointment findAppointmentById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Consulta", id));
    }

    private void checkPatientAccess(Appointment appointment) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.ROLE_PACIENTE) {
            Patient patient = patientRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new BusinessException("Paciente não encontrado para o usuário atual"));
            if (!appointment.getPaciente().getId().equals(patient.getId())) {
                throw new BusinessException("Acesso negado: você não pode visualizar consultas de outros pacientes");
            }
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuário autenticado não encontrado"));
    }
}
