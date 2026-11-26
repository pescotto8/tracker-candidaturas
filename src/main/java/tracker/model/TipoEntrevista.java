package tracker.model;

/** Tipo de una entrevista. */
public enum TipoEntrevista {
    TELEFONICA("Telefónica"),
    TECNICA("Técnica"),
    PRESENCIAL("Presencial"),
    ONLINE("Online");

    private final String nombre;

    TipoEntrevista(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve el tipo en minúsculas y sin tildes, tal y como se guarda en la base de datos.
     *
     * @return etiqueta del tipo
     */
    public String etiqueta() {
        return name().toLowerCase();
    }

    /**
     * Devuelve el nombre que se muestra al usuario.
     *
     * @return nombre del tipo
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Convierte un texto (sin distinguir mayúsculas ni tildes) en un tipo válido.
     *
     * @param texto nombre del tipo
     * @return tipo correspondiente
     * @throws tracker.exception.TrackerException si el texto no corresponde a ningún tipo
     */
    public static TipoEntrevista desdeTexto(String texto) {
        return Etiquetas.parsear(TipoEntrevista.class, texto, "Tipo de entrevista");
    }
}
