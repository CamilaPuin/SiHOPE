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
    rol_id              INT,
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT uk_usuario_codigo UNIQUE (codigo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS historial (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT,
    tipo        VARCHAR(50),
    descripcion VARCHAR(255),
    fecha       DATETIME,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO rol (nombre) VALUES
    ('ADMINISTRADOR'),
    ('COORDINADOR'),
    ('MONITOR'),
    ('ESTUDIANTE');
