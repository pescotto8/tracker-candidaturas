package com.impulso.model;

public enum ResultadoEntrevista {
    PENDIENTE("Pendiente"),
    POSITIVO("Positivo"),
    NEGATIVO("Negativo");

    private final String etiqueta;

    ResultadoEntrevista(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
