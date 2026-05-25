package com.hospital.notification.service;

import com.hospital.notification.domain.NotificationLog;
import com.hospital.notification.domain.NotificationLogRepository;
import com.hospital.notification.dto.AppointmentEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock NotificationLogRepository logRepository;
    @InjectMocks NotificationService notificationService;

    private AppointmentEvent buildEvent(String type) {
        AppointmentEvent event = new AppointmentEvent();
        event.setAppointmentId(UUID.randomUUID());
        event.setEventType(type);
        event.setDataConsulta(LocalDateTime.now().plusDays(2));
        event.setStatus("AGENDADA");
        event.setMedicoId(UUID.randomUUID());
        event.setMedicoNome("Dr. Silva");
        event.setMedicoEmail("medico@hospital.com");
        event.setPacienteId(UUID.randomUUID());
        event.setPacienteNome("João Paciente");
        event.setPacienteTelefone("(48) 99999-1234");
        event.setOccurredAt(LocalDateTime.now());
        return event;
    }

    @Test
    @DisplayName("processAppointmentCreated should save log with ENVIADA status")
    void shouldProcessCreatedEvent() {
        AppointmentEvent event = buildEvent("CREATED");
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.processAppointmentCreated(event);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());

        NotificationLog saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("ENVIADA");
        assertThat(saved.getEventType()).isEqualTo("CREATED");
        assertThat(saved.getPacienteNome()).isEqualTo("João Paciente");
        assertThat(saved.getMessage()).contains("AGENDADA");
    }

    @Test
    @DisplayName("processAppointmentCancelled should save log with cancel message")
    void shouldProcessCancelledEvent() {
        AppointmentEvent event = buildEvent("CANCELLED");
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.processAppointmentCancelled(event);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage()).contains("CANCELADA");
    }
}
