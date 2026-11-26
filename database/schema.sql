CREATE DATABASE IF NOT EXISTS impulso
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE impulso;

CREATE TABLE IF NOT EXISTS empresa (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL UNIQUE,
    sector         VARCHAR(100) NOT NULL,
    web            VARCHAR(255) NULL,
    fechaRegistro  DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS oferta (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa        INT          NOT NULL,
    titulo            VARCHAR(120) NOT NULL,
    descripcion       TEXT         NULL,
    estado            ENUM('pendiente', 'entrevista', 'rechazado', 'aceptado') NOT NULL DEFAULT 'pendiente',
    fechaPostulacion  DATE         NOT NULL,
    enlace            VARCHAR(255) NULL,
    salario           DECIMAL(10, 2) NULL,
    notas             TEXT         NULL,
    CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    INDEX idx_oferta_estado (estado),
    INDEX idx_oferta_fecha (fechaPostulacion)
);

CREATE TABLE IF NOT EXISTS entrevista (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_oferta  INT  NOT NULL,
    fecha      DATE NOT NULL,
    hora       TIME NULL,
    tipo       ENUM('telefonica', 'tecnica', 'presencial', 'online') NOT NULL,
    notas      TEXT NULL,
    resultado  ENUM('pendiente', 'positivo', 'negativo') NOT NULL DEFAULT 'pendiente',
    CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta) REFERENCES oferta (id) ON DELETE CASCADE,
    INDEX idx_entrevista_oferta (id_oferta)
);
