-- V1__initial_schema.sql

CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(150)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    senha       VARCHAR(255)        NOT NULL,
    role        VARCHAR(30)         NOT NULL,
    ativo       BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS patients (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome             VARCHAR(150)   NOT NULL,
    data_nascimento  DATE           NOT NULL,
    telefone         VARCHAR(20)    NOT NULL,
    cpf              VARCHAR(14)    NOT NULL UNIQUE,
    user_id          UUID           REFERENCES users(id),
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS appointments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_consulta   TIMESTAMP       NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'AGENDADA',
    descricao       TEXT,
    medico_id       UUID            NOT NULL REFERENCES users(id),
    paciente_id     UUID            NOT NULL REFERENCES patients(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_appointments_medico    ON appointments(medico_id);
CREATE INDEX idx_appointments_paciente  ON appointments(paciente_id);
CREATE INDEX idx_appointments_status    ON appointments(status);
CREATE INDEX idx_appointments_data      ON appointments(data_consulta);
CREATE INDEX idx_patients_user          ON patients(user_id);

-- Senha de todos: Hospital@123
INSERT INTO users (id, nome, email, senha, role) VALUES
    ('a1b2c3d4-0000-0000-0000-000000000001', 'Dr. Carlos Silva',    'medico@hospital.com',     '$2a$10$bPFRC79WzNutJc28blkmJOX92Eksv7YesAesOX28fo8M8Rfy1LF2m', 'ROLE_MEDICO'),
    ('a1b2c3d4-0000-0000-0000-000000000002', 'Enf. Ana Souza',      'enfermeiro@hospital.com', '$2a$10$8.OIXra1K5jMfFNTxf6CMelKr0A5R9VLAm.Uaocs5LTr8hzpvJ3eu', 'ROLE_ENFERMEIRO'),
    ('a1b2c3d4-0000-0000-0000-000000000003', 'João Paciente',       'paciente@hospital.com',   '$2a$10$dRa.p1MHaLVRochrGshh7.7rLq/y/hBklNuiH/i6J9MdjG1Yldaz.', 'ROLE_PACIENTE');

INSERT INTO patients (id, nome, data_nascimento, telefone, cpf, user_id) VALUES
    ('b1b2c3d4-0000-0000-0000-000000000001', 'João Paciente', '1985-03-15', '(48) 99999-1234', '123.456.789-00',
     'a1b2c3d4-0000-0000-0000-000000000003');
