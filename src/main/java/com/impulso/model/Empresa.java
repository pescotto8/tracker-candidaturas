package com.impulso.model;

import java.time.LocalDate;

public class Empresa {
    private int id;
    private String nombre;
    private String sector;
    private String web;
    private LocalDate fechaRegistro;
    /** Calculado con JOIN/COUNT; no se persiste. */
    private int numCandidaturas;

    public Empresa() {
    }

    public Empresa(int id, String nombre, String sector, String web, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.sector = sector;
        this.web = web;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public int getNumCandidaturas() { return numCandidaturas; }
    public void setNumCandidaturas(int numCandidaturas) { this.numCandidaturas = numCandidaturas; }

    /** Iniciales para el avatar (IN, NT...). */
    public String getIniciales() {
        String n = nombre == null ? "" : nombre.trim();
        return n.length() >= 2 ? n.substring(0, 2).toUpperCase() : n.toUpperCase();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
