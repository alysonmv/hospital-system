-- V1__notification_schema.sql

CREATE TABLE IF NOT EXISTS notification_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id  UUID            NOT NULL,
    event_type      VARCHAR(30)     NOT NULL,
    paciente_nome   VARCHAR(150),
    paciente_tel    VARCHAR(20),
    medico_nome     VARCHAR(150),
    data_consulta   TIMESTAMP,
    status          VARCHAR(30)     NOT NULL DEFAULT 'ENVIADA',
    message         TEXT,
    error_message   TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_appointment ON notification_logs(appointment_id);
CREATE INDEX idx_notification_status      ON notification_logs(status);
CREATE INDEX idx_notification_created     ON notification_logs(created_at);
