package tracker.model;

import java.time.LocalDate;

/** Candidatura a una oferta de empleo. */
public record Candidatura(
        long id,
        String titulo,
        String empresa,
        Estado estado,
        LocalDate fechaPostulacion,
        String enlace,
        String notas) {

    public Candidatura conId(long nuevoId) {
        return new Candidatura(nuevoId, titulo, empresa, estado, fechaPostulacion, enlace, notas);
    }
}
