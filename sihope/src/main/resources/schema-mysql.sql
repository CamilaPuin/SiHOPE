CREATE TABLE IF NOT EXISTS rol (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuario (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombres             VARCHAR(100),
    apellidos           VARCHAR(100),
    codigo              VARCHAR(50),
    correo              VARCHAR(150),
    password            VARCHAR(100),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    verificado          BOOLEAN NOT NULL DEFAULT FALSE,
    token_verificacion  VARCHAR(100),
    token_reset         VARCHAR(100),
    token_reset_expira  DATETIME,
    token_version       INT NOT NULL DEFAULT 0,
    rol_id              INT,
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT uk_usuario_codigo UNIQUE (codigo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alta idempotente de token_version para bases de datos creadas antes del Sprint 2.
-- (MySQL 8 no soporta ADD COLUMN IF NOT EXISTS; se resuelve con SQL dinámico.)
SET @col_existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'usuario'
                     AND COLUMN_NAME = 'token_version');
SET @ddl_token_version = IF(@col_existe = 0,
    'ALTER TABLE usuario ADD COLUMN token_version INT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt_token_version FROM @ddl_token_version;
EXECUTE stmt_token_version;
DEALLOCATE PREPARE stmt_token_version;

CREATE TABLE IF NOT EXISTS historial (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT,
    tipo        VARCHAR(50),
    descripcion VARCHAR(255),
    fecha       DATETIME,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Sprint 2 · HU_006: disponibilidad horaria semanal del monitor
-- Un registro por bloque (día de la semana + franja horaria).
-- ============================================================
CREATE TABLE IF NOT EXISTS disponibilidad (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    monitor_id  INT NOT NULL,
    dia_semana  TINYINT NOT NULL,           -- 1 = Lunes ... 7 = Domingo
    hora_inicio TIME NOT NULL,
    hora_fin    TIME NOT NULL,
    CONSTRAINT fk_disp_monitor FOREIGN KEY (monitor_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Sprint 2 · HU_008 / HU_005: convocatorias de selección de monitores
-- ============================================================
CREATE TABLE IF NOT EXISTS convocatoria (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    titulo         VARCHAR(150) NOT NULL,
    descripcion    TEXT,
    requisitos     TEXT,
    materia        VARCHAR(150) NOT NULL,
    plazas         INT NOT NULL,
    fecha_limite   DATE NOT NULL,
    estado         VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',   -- ABIERTA | CERRADA
    fecha_creacion DATETIME NOT NULL,
    coordinador_id INT,
    CONSTRAINT fk_conv_coordinador FOREIGN KEY (coordinador_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Sprint 2 · HU_005 / HU_009: postulaciones de aspirantes a monitor
-- datos_json guarda los campos parametrizables del formulario de postulación.
-- ============================================================
CREATE TABLE IF NOT EXISTS postulacion (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    convocatoria_id   INT NOT NULL,
    aspirante_id      INT NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',  -- PENDIENTE | APROBADA | RECHAZADA
    datos_json        TEXT,
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
