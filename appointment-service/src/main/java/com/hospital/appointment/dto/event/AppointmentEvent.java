package com.hospital.appointment.dto.event;

import com.hospital.appointment.domain.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AppointmentEvent {

    private UUID appointmentId;
    private String eventType;           // CREATED, UPDATED, CANCELLED
    private LocalDateTime dataConsulta;
    private AppointmentStatus status;
    private String descricao;

    private UUID medicoId;
    private String medicoNome;
    private String medicoEmail;

    private UUID pacienteId;
    private String pacienteNome;
    private String pacienteTelefone;

    private LocalDateTime occurredAt;

    public static AppointmentEvent of(String eventType, UUID appointmentId,
                                       LocalDateTime dataConsulta, AppointmentStatus status,
                                       String descricao,
                                       UUID medicoId, String medicoNome, String medicoEmail,
                                       UUID pacienteId, String pacienteNome, String pacienteTelefone) {
        return AppointmentEvent.builder()
                .eventType(eventType)
                .appointmentId(appointmentId)
                .dataConsulta(dataConsulta)
                .status(status)
                .descricao(descricao)
                .medicoId(medicoId)
                .medicoNome(medicoNome)
                .medicoEmail(medicoEmail)
                .pacienteId(pacienteId)
                .pacienteNome(pacienteNome)
                .pacienteTelefone(pacienteTelefone)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
