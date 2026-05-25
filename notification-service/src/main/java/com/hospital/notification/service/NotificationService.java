package com.hospital.notification.service;

import com.hospital.notification.domain.NotificationLog;
import com.hospital.notification.domain.NotificationLogRepository;
import com.hospital.notification.dto.AppointmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final NotificationLogRepository logRepository;

    @Transactional
    public void processAppointmentCreated(AppointmentEvent event) {
        String message = buildCreatedMessage(event);
        sendNotification(event, message);
    }

    @Transactional
    public void processAppointmentUpdated(AppointmentEvent event) {
        String message = buildUpdatedMessage(event);
        sendNotification(event, message);
    }

    @Transactional
    public void processAppointmentCancelled(AppointmentEvent event) {
        String message = buildCancelledMessage(event);
        sendNotification(event, message);
    }

    private void sendNotification(AppointmentEvent event, String message) {
        // Simulação de envio (SMS / Email / WhatsApp)
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📬 NOTIFICAÇÃO ENVIADA");
        log.info("Para:     {} | {}", event.getPacienteNome(), event.getPacienteTelefone());
        log.info("Evento:   {}", event.getEventType());
        log.info("Mensagem: {}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        NotificationLog notificationLog = NotificationLog.builder()
                .appointmentId(event.getAppointmentId())
                .eventType(event.getEventType())
                .pacienteNome(event.getPacienteNome())
                .pacienteTel(event.getPacienteTelefone())
                .medicoNome(event.getMedicoNome())
                .dataConsulta(event.getDataConsulta())
                .status("ENVIADA")
                .message(message)
                .build();

        logRepository.save(notificationLog);
    }

    private String buildCreatedMessage(AppointmentEvent event) {
        return String.format(
                "Olá, %s! Sua consulta foi AGENDADA com Dr(a). %s para %s. " +
                "Em caso de dúvidas, entre em contato com o hospital.",
                event.getPacienteNome(),
                event.getMedicoNome(),
                event.getDataConsulta().format(FORMATTER)
        );
    }

    private String buildUpdatedMessage(AppointmentEvent event) {
        return String.format(
                "Olá, %s! Sua consulta com Dr(a). %s foi ATUALIZADA. " +
                "Nova data/hora: %s. Status: %s.",
                event.getPacienteNome(),
                event.getMedicoNome(),
                event.getDataConsulta().format(FORMATTER),
                event.getStatus()
        );
    }

    private String buildCancelledMessage(AppointmentEvent event) {
        return String.format(
                "Olá, %s! Sua consulta com Dr(a). %s agendada para %s foi CANCELADA. " +
                "Por favor, entre em contato para reagendar.",
                event.getPacienteNome(),
                event.getMedicoNome(),
                event.getDataConsulta().format(FORMATTER)
        );
    }
}
