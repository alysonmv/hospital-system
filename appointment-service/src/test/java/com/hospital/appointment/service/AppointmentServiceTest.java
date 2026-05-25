package com.hospital.appointment.service;

import com.hospital.appointment.domain.entity.Appointment;
import com.hospital.appointment.domain.entity.Patient;
import com.hospital.appointment.domain.entity.User;
import com.hospital.appointment.domain.enums.AppointmentStatus;
import com.hospital.appointment.domain.enums.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock UserRepository userRepository;
    @Mock PatientRepository patientRepository;
    @Mock AppointmentMapper appointmentMapper;
    @Mock AppointmentEventProducer eventProducer;

    @InjectMocks
    AppointmentService appointmentService;

    private User medico;
    private Patient paciente;
    private Appointment appointment;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        medico = User.builder()
                .id(UUID.randomUUID())
                .nome("Dr. Silva")
                .email("medico@hospital.com")
                .role(Role.ROLE_MEDICO)
                .ativo(true)
                .build();

        paciente = Patient.builder()
                .id(UUID.randomUUID())
                .nome("João")
                .telefone("(48) 99999-0000")
                .build();

        appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .dataConsulta(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.AGENDADA)
                .medico(medico)
                .paciente(paciente)
                .build();

        appointmentResponse = new AppointmentResponse(
                appointment.getId(),
                appointment.getDataConsulta(),
                AppointmentStatus.AGENDADA,
                null,
                new AppointmentResponse.UserSummary(medico.getId(), medico.getNome(), medico.getEmail()),
                new AppointmentResponse.PatientSummary(paciente.getId(), paciente.getNome(), paciente.getTelefone()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Mock security context
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(medico.getEmail());
        SecurityContextHolder.setContext(ctx);
        when(userRepository.findByEmail(medico.getEmail())).thenReturn(Optional.of(medico));
    }

    @Test
    @DisplayName("Should create appointment successfully")
    void shouldCreateAppointment() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                LocalDateTime.now().plusDays(1),
                medico.getId(),
                paciente.getId(),
                "Consulta de rotina"
        );

        when(userRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(patientRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(appointmentMapper.toResponse(any())).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(AppointmentStatus.AGENDADA);
        verify(appointmentRepository).save(any());
        verify(eventProducer).publishCreated(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when doctor not found or invalid role")
    void shouldThrowWhenMedicoInvalid() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                LocalDateTime.now().plusDays(1),
                UUID.randomUUID(),
                paciente.getId(),
                null
        );

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Médico");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient not found")
    void shouldThrowWhenPatientNotFound() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                LocalDateTime.now().plusDays(1),
                medico.getId(),
                UUID.randomUUID(),
                null
        );

        when(userRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(patientRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should cancel appointment and publish event")
    void shouldCancelAppointment() {
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        appointmentService.cancel(appointment.getId());

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELADA);
        verify(eventProducer).publishCancelled(any());
    }

    @Test
    @DisplayName("Should throw when cancelling already cancelled appointment")
    void shouldThrowWhenCancellingCancelledAppointment() {
        appointment.setStatus(AppointmentStatus.CANCELADA);
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(appointment.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    @DisplayName("Should throw when updating a cancelled appointment")
    void shouldThrowWhenUpdatingCancelledAppointment() {
        appointment.setStatus(AppointmentStatus.CANCELADA);
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(null, null, "Nova desc");

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.update(appointment.getId(), request))
                .isInstanceOf(BusinessException.class);
    }
}
