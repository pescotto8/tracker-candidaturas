package com.impulso.model;

import java.time.LocalDate;

public class Oferta {
    private int id;
    private int idEmpresa;
    private String titulo;
    private String descripcion;
    private Estado estado = Estado.PENDIENTE;
    private LocalDate fechaPostulacion;
    private String enlace;
    private Double salario;
    private String notas;

    // Campos de JOIN con empresa (solo lectura)
    private String empresaNombre;
    private String empresaSector;
    private String empresaWeb;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDate fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }
    public String getEnlace() { return enlace; }
    public void setEnlace(String enlace) { this.enlace = enlace; }
    public Double getSalario() { return salario; }
    public void setSalario(Double salario) { this.salario = salario; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }
    public String getEmpresaSector() { return empresaSector; }
    public void setEmpresaSector(String empresaSector) { this.empresaSector = empresaSector; }
    public String getEmpresaWeb() { return empresaWeb; }
    public void setEmpresaWeb(String empresaWeb) { this.empresaWeb = empresaWeb; }
}
