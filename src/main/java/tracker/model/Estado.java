package tracker.model;

/** Estado de una candidatura. */
public enum Estado {
    PENDIENTE("Pendiente"),
    ENTREVISTA("Entrevista"),
    RECHAZADO("Rechazado"),
    ACEPTADO("Aceptado");

    private final String nombre;

    Estado(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve el estado en minúsculas y sin tildes, tal y como se guarda en la base de datos.
     *
     * @return etiqueta del estado
     */
    public String etiqueta() {
        return name().toLowerCase();
    }

    /**
     * Devuelve el nombre que se muestra al usuario.
     *
     * @return nombre del estado
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Convierte un texto (sin distinguir mayúsculas ni tildes) en un estado válido.
     *
     * @param texto nombre del estado
     * @return estado correspondiente
     * @throws tracker.exception.TrackerException si el texto no corresponde a ningún estado
     */
    public static Estado desdeTexto(String texto) {
        return Etiquetas.parsear(Estado.class, texto, "Estado");
    }
}
