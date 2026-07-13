CREATE TABLE IF NOT EXISTS rol (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    codigo VARCHAR(50),
    correo VARCHAR(150),
    password VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    verificado BOOLEAN NOT NULL DEFAULT FALSE,
    token_verificacion VARCHAR(100),
    token_reset VARCHAR(100),
    token_reset_expira DATETIME,
    token_version INT NOT NULL DEFAULT 0,
    rol_id INT,
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT uk_usuario_codigo UNIQUE (codigo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS historial (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    tipo VARCHAR(50),
    descripcion VARCHAR(255),
    fecha DATETIME,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS disponibilidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    monitor_id INT NOT NULL,
    dia_semana TINYINT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CONSTRAINT fk_disp_monitor FOREIGN KEY (monitor_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS convocatoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    requisitos TEXT,
    materia VARCHAR(150) NOT NULL,
    plazas INT NOT NULL,
    fecha_limite DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    fecha_creacion DATETIME NOT NULL,
    coordinador_id INT,
    CONSTRAINT fk_conv_coordinador FOREIGN KEY (coordinador_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS postulacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    convocatoria_id INT NOT NULL,
    aspirante_id INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    datos_json TEXT,
    fecha_postulacion DATETIME NOT NULL,
    CONSTRAINT fk_post_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES convocatoria (id),
    CONSTRAINT fk_post_aspirante FOREIGN KEY (aspirante_id) REFERENCES usuario (id),
    CONSTRAINT uk_post_conv_aspirante UNIQUE (convocatoria_id, aspirante_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO rol (nombre) VALUES
    ('ADMINISTRADOR'),
    ('COORDINADOR'),
    ('MONITOR'),
    ('ESTUDIANTE');

-- Catálogo de asignaturas/temáticas (Paso 0). El monitor puede agregar las suyas
-- desde su perfil; también se auto-ingieren las materias de convocatorias existentes.
CREATE TABLE IF NOT EXISTS asignatura (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50),
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT uk_asignatura_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Relación monitor↔asignatura (sustenta el filtro de HU_004 y la validación de HU_002).
CREATE TABLE IF NOT EXISTS monitor_asignatura (
    monitor_id INT NOT NULL,
    asignatura_id INT NOT NULL,
    PRIMARY KEY (monitor_id, asignatura_id),
    CONSTRAINT fk_ma_monitor FOREIGN KEY (monitor_id) REFERENCES usuario (id),
    CONSTRAINT fk_ma_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignatura (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Semilla inicial del catálogo (idempotente).
INSERT IGNORE INTO asignatura (nombre) VALUES
    ('Cálculo Diferencial'),
    ('Cálculo Integral'),
    ('Álgebra Lineal'),
    ('Física Mecánica'),
    ('Programación I'),
    ('Estructuras de Datos'),
    ('Bases de Datos'),
    ('Química General');

-- Citas/monitorías agendadas (HU_002). slot_key con índice UNIQUE evita agendar dos
-- citas activas en el mismo horario; se pone a NULL al cancelar para liberar el cupo
-- (MySQL permite múltiples NULL en un índice único).
CREATE TABLE IF NOT EXISTS cita (
    id INT AUTO_INCREMENT PRIMARY KEY,
    estudiante_id INT NOT NULL,
    monitor_id INT NOT NULL,
    asignatura_id INT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'RESERVADA',
    motivo_cancelacion VARCHAR(255),
    recordatorio_enviado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME NOT NULL,
    slot_key VARCHAR(80),
    CONSTRAINT fk_cita_estudiante FOREIGN KEY (estudiante_id) REFERENCES usuario (id),
    CONSTRAINT fk_cita_monitor FOREIGN KEY (monitor_id) REFERENCES usuario (id),
    CONSTRAINT fk_cita_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignatura (id),
    CONSTRAINT uk_cita_slot UNIQUE (slot_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;