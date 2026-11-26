package tracker.model;

/** Resultado de una entrevista. */
public enum ResultadoEntrevista {
    PENDIENTE("Pendiente"),
    POSITIVO("Positivo"),
    NEGATIVO("Negativo");

    private final String nombre;

    ResultadoEntrevista(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve el resultado en minúsculas, tal y como se guarda en la base de datos.
     *
     * @return etiqueta del resultado
     */
    public String etiqueta() {
        return name().toLowerCase();
    }

    /**
     * Devuelve el nombre que se muestra al usuario.
     *
     * @return nombre del resultado
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Convierte un texto (sin distinguir mayúsculas ni tildes) en un resultado válido.
     *
     * @param texto nombre del resultado
     * @return resultado correspondiente
     * @throws tracker.exception.TrackerException si el texto no corresponde a ningún resultado
     */
    public static ResultadoEntrevista desdeTexto(String texto) {
        return Etiquetas.parsear(ResultadoEntrevista.class, texto, "Resultado");
    }
}
