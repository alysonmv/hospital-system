package com.hospital.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentEvent {
    private UUID appointmentId;
    private String eventType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataConsulta;
    private String status;
    private String descricao;
    private UUID medicoId;
    private String medicoNome;
    private String medicoEmail;
    private UUID pacienteId;
    private String pacienteNome;
    private String pacienteTelefone;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime occurredAt;
}
