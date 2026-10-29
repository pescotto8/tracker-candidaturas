package tracker.model;

import java.time.LocalDate;

/**
 * Candidatura a una oferta de empleo.
 *
 * @param id identificador único
 * @param titulo título de la oferta
 * @param empresa nombre de la empresa
 * @param estado estado actual de la candidatura
 * @param fechaPostulacion fecha en la que se presentó la candidatura
 * @param enlace URL de la oferta (puede estar vacía)
 * @param notas notas personales (pueden estar vacías)
 */
public record Candidatura(
        long id,
        String titulo,
        String empresa,
        Estado estado,
        LocalDate fechaPostulacion,
        String enlace,
        String notas) {

    /**
     * Crea una copia de la candidatura con otro identificador.
     *
     * @param nuevoId identificador de la copia
     * @return nueva candidatura con el mismo contenido y el ID indicado
     */
    public Candidatura conId(long nuevoId) {
        return new Candidatura(nuevoId, titulo, empresa, estado, fechaPostulacion, enlace, notas);
    }
}
