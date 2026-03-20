-- ══════════════════════════════════════════════════════════════
--  V1__esquema_inicial.sql
--  Flyway migration — Propiedad Residencia
-- ══════════════════════════════════════════════════════════════

-- ── EXTENSIONES ──────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── UNIDADES ──────────────────────────────────────────────────
CREATE TABLE unidades (
    id              BIGSERIAL PRIMARY KEY,
    numero          VARCHAR(10)  NOT NULL,
    torre           VARCHAR(20)  NOT NULL,
    piso            INTEGER      NOT NULL,
    tipo            VARCHAR(20)  NOT NULL CHECK (tipo IN ('PROPIETARIO','ARRENDATARIO')),
    ocupada         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_ingreso   DATE,
    estado_cuota    VARCHAR(20)  NOT NULL DEFAULT 'AL_DIA'
                        CHECK (estado_cuota IN ('AL_DIA','PENDIENTE','MOROSO')),
    creado_en       TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT unidades_numero_torre_uq UNIQUE (numero, torre)
);

-- ── USUARIOS ──────────────────────────────────────────────────
CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    telefono        VARCHAR(20)  NOT NULL,
    rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('RESIDENTE','ADMIN','PORTERIA')),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    unidad_id       BIGINT       REFERENCES unidades(id) ON DELETE SET NULL,
    creado_en       TIMESTAMP    NOT NULL DEFAULT NOW(),
    actualizado_en  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_email   ON usuarios(email);
CREATE INDEX idx_usuarios_unidad  ON usuarios(unidad_id);
CREATE INDEX idx_usuarios_rol     ON usuarios(rol);

-- ── SOLICITUDES ───────────────────────────────────────────────
CREATE TABLE solicitudes (
    id              BIGSERIAL PRIMARY KEY,
    numero          VARCHAR(20)  NOT NULL UNIQUE,   -- SOL-2025-0001
    titulo          VARCHAR(255) NOT NULL,
    descripcion     TEXT,
    tipo            VARCHAR(30)  NOT NULL
                        CHECK (tipo IN ('MANTENIMIENTO','QUEJA','RESERVA','VISITA','PAGO','OTRO')),
    prioridad       VARCHAR(10)  NOT NULL DEFAULT 'MEDIA'
                        CHECK (prioridad IN ('BAJA','MEDIA','ALTA')),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('SIN_ASIGNAR','PENDIENTE','EN_PROCESO','RESUELTO','CANCELADO')),
    ubicacion       VARCHAR(100),
    residente_id    BIGINT       NOT NULL REFERENCES usuarios(id),
    asignado_a_id   BIGINT       REFERENCES usuarios(id),
    unidad_id       BIGINT       REFERENCES unidades(id),
    creado_en       TIMESTAMP    NOT NULL DEFAULT NOW(),
    actualizado_en  TIMESTAMP    NOT NULL DEFAULT NOW(),
    resuelta_en     TIMESTAMP,
    asignada_en     TIMESTAMP
);

CREATE INDEX idx_solicitudes_residente  ON solicitudes(residente_id);
CREATE INDEX idx_solicitudes_estado     ON solicitudes(estado);
CREATE INDEX idx_solicitudes_tipo       ON solicitudes(tipo);
CREATE INDEX idx_solicitudes_asignado   ON solicitudes(asignado_a_id);
CREATE INDEX idx_solicitudes_creado     ON solicitudes(creado_en DESC);

-- Archivos adjuntos de solicitudes
CREATE TABLE solicitud_archivos (
    solicitud_id    BIGINT      NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
    url_archivo     VARCHAR(500) NOT NULL
);

-- ── COMENTARIOS DE SOLICITUD ──────────────────────────────────
CREATE TABLE comentarios_solicitud (
    id              BIGSERIAL PRIMARY KEY,
    contenido       TEXT         NOT NULL,
    solicitud_id    BIGINT       NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
    autor_id        BIGINT       NOT NULL REFERENCES usuarios(id),
    creado_en       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comentarios_solicitud ON comentarios_solicitud(solicitud_id);

-- ── RESERVAS ──────────────────────────────────────────────────
CREATE TABLE reservas (
    id              BIGSERIAL PRIMARY KEY,
    espacio         VARCHAR(30)  NOT NULL
                        CHECK (espacio IN ('SALON_COMUNAL','PISCINA','ZONA_BBQ',
                                           'CANCHA_MULTIPLE','CANCHA_TENIS','GIMNASIO')),
    fecha           DATE         NOT NULL,
    hora_inicio     TIME         NOT NULL,
    hora_fin        TIME         NOT NULL,
    numero_personas INTEGER,
    motivo          TEXT,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMADA'
                        CHECK (estado IN ('CONFIRMADA','CANCELADA','PASADA')),
    residente_id    BIGINT       NOT NULL REFERENCES usuarios(id),
    unidad_id       BIGINT       REFERENCES unidades(id),
    creado_en       TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- Evitar doble reserva del mismo espacio en el mismo horario
    CONSTRAINT reservas_no_overlap EXCLUDE USING gist (
        espacio WITH =,
        tsrange(fecha + hora_inicio, fecha + hora_fin) WITH &&
    ) WHERE (estado = 'CONFIRMADA')
);

CREATE INDEX idx_reservas_residente ON reservas(residente_id);
CREATE INDEX idx_reservas_fecha     ON reservas(fecha);
CREATE INDEX idx_reservas_espacio   ON reservas(espacio);

-- ── REGISTROS DE ACCESO (Portería) ────────────────────────────
CREATE TABLE registros_acceso (
    id                  BIGSERIAL PRIMARY KEY,
    tipo                VARCHAR(20)  NOT NULL
                            CHECK (tipo IN ('ENTRADA','SALIDA','DELIVERY','TAXI')),
    nombre_visitante    VARCHAR(120) NOT NULL,
    documento           VARCHAR(30),
    placa_vehiculo      VARCHAR(15),
    destino_apto        VARCHAR(50)  NOT NULL,
    portero_id          BIGINT       REFERENCES usuarios(id),
    fecha_hora          TIMESTAMP    NOT NULL DEFAULT NOW(),
    autorizado          BOOLEAN      NOT NULL DEFAULT TRUE,
    observaciones       TEXT
);

CREATE INDEX idx_accesos_fecha     ON registros_acceso(fecha_hora DESC);
CREATE INDEX idx_accesos_tipo      ON registros_acceso(tipo);
CREATE INDEX idx_accesos_portero   ON registros_acceso(portero_id);

-- ── FUNCIÓN: auto-actualizar updated_at ───────────────────────
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.actualizado_en = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp_usuarios
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_solicitudes
    BEFORE UPDATE ON solicitudes
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- ── FUNCIÓN: generar número de solicitud ─────────────────────
CREATE OR REPLACE FUNCTION generar_numero_solicitud()
RETURNS TRIGGER AS $$
BEGIN
  NEW.numero = 'SOL-' || TO_CHAR(NOW(), 'YYYY') || '-' ||
               LPAD(NEW.id::TEXT, 4, '0');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_numero_solicitud
    BEFORE INSERT ON solicitudes
    FOR EACH ROW EXECUTE FUNCTION generar_numero_solicitud();

-- ══════════════════════════════════════════════════════════════
--  DATOS INICIALES
-- ══════════════════════════════════════════════════════════════

-- Unidades de ejemplo
INSERT INTO unidades (numero, torre, piso, tipo, fecha_ingreso) VALUES
('101', 'Torre A', 1, 'PROPIETARIO',   '2019-03-01'),
('104', 'Torre A', 1, 'ARRENDATARIO',  '2022-03-18'),
('201', 'Torre A', 2, 'ARRENDATARIO',  '2023-07-11'),
('210', 'Torre A', 2, 'PROPIETARIO',   '2019-11-05'),
('301', 'Torre A', 3, 'PROPIETARIO',   '2021-05-10'),
('310', 'Torre A', 3, 'PROPIETARIO',   '2020-08-22'),
('405', 'Torre B', 4, 'PROPIETARIO',   '2025-02-20'),
('502', 'Torre B', 5, 'ARRENDATARIO',  '2023-01-15');

-- Usuario admin por defecto (password: Admin1234!)
-- Hash BCrypt generado: use un generador online o Spring en primer boot
INSERT INTO usuarios (nombre, email, password, telefono, rol) VALUES
('María Álvarez', 'admin@residencia.co',
 '$2a$12$LQv3c1yqBWVHxkd0LQ1Ns.WVCIqNdlJXGkFJVXX2hK7K8P3N6l9rO',
 '+57 300 000 0001', 'ADMIN');

-- Usuario portería (password: Porteria1!)
INSERT INTO usuarios (nombre, email, password, telefono, rol) VALUES
('Carlos Torres', 'porteria@residencia.co',
 '$2a$12$AnotherHashExample000000000000000000000000000000000000',
 '+57 300 000 0002', 'PORTERIA');
