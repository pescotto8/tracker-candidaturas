-- Impulso v2.0 · Esquema MySQL 8 (utf8mb4)
-- Uso:  mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS impulso
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE impulso;

CREATE TABLE IF NOT EXISTS empresa (
    id              INT           NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100)  NOT NULL,
    sector          VARCHAR(100)  NOT NULL,
    web             VARCHAR(255)  NULL,
    fecha_registro  DATE          NOT NULL,
    CONSTRAINT pk_empresa PRIMARY KEY (id),
    CONSTRAINT uq_empresa_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS oferta (
    id                 INT            NOT NULL AUTO_INCREMENT,
    id_empresa         INT            NOT NULL,
    titulo             VARCHAR(120)   NOT NULL,
    descripcion        TEXT           NULL,
    estado             ENUM('PENDIENTE','ENTREVISTA','ACEPTADO','RECHAZADO')
                                      NOT NULL DEFAULT 'PENDIENTE',
    fecha_postulacion  DATE           NOT NULL,
    enlace             VARCHAR(500)   NULL,
    salario            DECIMAL(10,2)  NULL,
    notas              TEXT           NULL,
    CONSTRAINT pk_oferta PRIMARY KEY (id),
    CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa)
        REFERENCES empresa (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_oferta_salario CHECK (salario IS NULL OR salario >= 0),
    INDEX idx_oferta_empresa (id_empresa),
    INDEX idx_oferta_estado (estado),
    INDEX idx_oferta_fecha (fecha_postulacion)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS entrevista (
    id          INT   NOT NULL AUTO_INCREMENT,
    id_oferta   INT   NOT NULL,
    fecha       DATE  NOT NULL,
    hora        TIME  NULL,                 -- opcional (campo "Hora" del prototipo)
    tipo        ENUM('TELEFONICA','TECNICA','RRHH','FINAL','OTRA')
                      NOT NULL,
    notas       TEXT  NULL,
    resultado   ENUM('PENDIENTE','POSITIVO','NEGATIVO')
                      NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_entrevista PRIMARY KEY (id),
    CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta)
        REFERENCES oferta (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_entrevista_oferta (id_oferta)
) ENGINE = InnoDB;
