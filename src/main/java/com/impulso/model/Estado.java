package com.impulso.model;

/** Estado de una candidatura (columna oferta.estado). */
public enum Estado {
    PENDIENTE("Pendiente"),
    ENTREVISTA("Entrevista"),
    ACEPTADO("Aceptado"),
    RECHAZADO("Rechazado");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Clase CSS usada para pintar la "píldora" de estado. */
    public String getCssClass() {
        return "estado-" + name().toLowerCase();
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
