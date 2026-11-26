package tracker.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entrevista de una oferta.
 *
 * @param id identificador; 0 si todavía no se ha guardado
 * @param idOferta identificador de la oferta a la que pertenece
 * @param fecha día de la entrevista
 * @param hora hora de la entrevista; {@code null} si no se conoce
 * @param tipo tipo de entrevista
 * @param notas notas de la entrevista (pueden estar vacías)
 * @param resultado resultado de la entrevista
 */
public record Entrevista(
        long id,
        long idOferta,
        LocalDate fecha,
        LocalTime hora,
        TipoEntrevista tipo,
        String notas,
        ResultadoEntrevista resultado) {

    /**
     * Indica si la entrevista todavía no se ha guardado.
     *
     * @return {@code true} si no tiene identificador
     */
    public boolean esNueva() {
        return id == 0;
    }

    /**
     * Crea una copia de la entrevista con otro identificador.
     *
     * @param nuevoId identificador de la copia
     * @return nueva entrevista con el mismo contenido y el ID indicado
     */
    public Entrevista conId(long nuevoId) {
        return new Entrevista(nuevoId, idOferta, fecha, hora, tipo, notas, resultado);
    }
}
