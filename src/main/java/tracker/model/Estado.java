package tracker.model;

import tracker.exception.TrackerException;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Estado de una candidatura. */
public enum Estado {
    PENDIENTE,
    ENTREVISTA,
    RECHAZADO,
    ACEPTADO;

    /**
     * Devuelve el estado en minúsculas, tal y como se guarda en el archivo.
     *
     * @return etiqueta del estado
     */
    public String etiqueta() {
        return name().toLowerCase();
    }

    /**
     * Convierte un texto (sin distinguir mayúsculas) en un estado válido.
     *
     * @param texto nombre del estado
     * @return estado correspondiente
     * @throws TrackerException si el texto no corresponde a ningún estado
     */
    public static Estado desdeTexto(String texto) {
        for (Estado estado : values()) {
            if (estado.etiqueta().equalsIgnoreCase(texto == null ? "" : texto.trim())) {
                return estado;
            }
        }
        String validos = Arrays.stream(values()).map(Estado::etiqueta).collect(Collectors.joining(", "));
        throw new TrackerException("Estado no válido: '" + texto + "'. Valores posibles: " + validos);
    }
}
