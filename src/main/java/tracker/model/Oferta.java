package tracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Oferta de empleo a la que se ha presentado una candidatura.
 *
 * @param id identificador; 0 si todavía no se ha guardado
 * @param empresa empresa que publica la oferta
 * @param titulo título de la oferta
 * @param descripcion descripción del puesto (puede estar vacía)
 * @param estado estado actual de la candidatura
 * @param fechaPostulacion fecha en la que se presentó la candidatura
 * @param enlace URL de la oferta (puede estar vacía)
 * @param salario salario anual en euros; {@code null} si no se conoce
 * @param notas notas personales (pueden estar vacías)
 */
public record Oferta(
        long id,
        Empresa empresa,
        String titulo,
        String descripcion,
        Estado estado,
        LocalDate fechaPostulacion,
        String enlace,
        BigDecimal salario,
        String notas) {

    /**
     * Indica si la oferta todavía no se ha guardado.
     *
     * @return {@code true} si no tiene identificador
     */
    public boolean esNueva() {
        return id == 0;
    }

    /**
     * Crea una copia de la oferta con otro identificador.
     *
     * @param nuevoId identificador de la copia
     * @return nueva oferta con el mismo contenido y el ID indicado
     */
    public Oferta conId(long nuevoId) {
        return new Oferta(nuevoId, empresa, titulo, descripcion, estado, fechaPostulacion, enlace, salario, notas);
    }

    /**
     * Crea una copia de la oferta con otro estado.
     *
     * @param nuevoEstado estado de la copia
     * @return nueva oferta con el mismo contenido y el estado indicado
     */
    public Oferta conEstado(Estado nuevoEstado) {
        return new Oferta(id, empresa, titulo, descripcion, nuevoEstado, fechaPostulacion, enlace, salario, notas);
    }
}
