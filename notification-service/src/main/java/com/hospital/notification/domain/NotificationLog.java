package com.hospital.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "paciente_nome", length = 150)
    private String pacienteNome;

    @Column(name = "paciente_tel", length = 20)
    private String pacienteTel;

    @Column(name = "medico_nome", length = 150)
    private String medicoNome;

    @Column(name = "data_consulta")
    private LocalDateTime dataConsulta;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "ENVIADA";

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
