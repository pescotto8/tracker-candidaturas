package com.impulso.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Entrevista {
    private int id;
    private int idOferta;
    private LocalDate fecha;
    private LocalTime hora;
    private TipoEntrevista tipo = TipoEntrevista.TELEFONICA;
    private String notas;
    private ResultadoEntrevista resultado = ResultadoEntrevista.PENDIENTE;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOferta() { return idOferta; }
    public void setIdOferta(int idOferta) { this.idOferta = idOferta; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public TipoEntrevista getTipo() { return tipo; }
    public void setTipo(TipoEntrevista tipo) { this.tipo = tipo; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public ResultadoEntrevista getResultado() { return resultado; }
    public void setResultado(ResultadoEntrevista resultado) { this.resultado = resultado; }
}
