package com.impulso.model;

public enum TipoEntrevista {
    TELEFONICA("Telefónica"),
    TECNICA("Técnica"),
    RRHH("RR. HH."),
    FINAL("Final"),
    OTRA("Otra");

    private final String etiqueta;

    TipoEntrevista(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
