package tracker.model;

import java.time.LocalDate;

/**
 * Empresa que publica ofertas de empleo.
 *
 * @param id identificador; 0 si todavía no se ha guardado
 * @param nombre nombre de la empresa
 * @param sector sector de actividad
 * @param web página web (puede estar vacía)
 * @param fechaRegistro fecha en la que se registró la empresa
 */
public record Empresa(long id, String nombre, String sector, String web, LocalDate fechaRegistro) {

    /**
     * Indica si la empresa todavía no se ha guardado.
     *
     * @return {@code true} si no tiene identificador
     */
    public boolean esNueva() {
        return id == 0;
    }

    /**
     * Crea una copia de la empresa con otro identificador.
     *
     * @param nuevoId identificador de la copia
     * @return nueva empresa con el mismo contenido y el ID indicado
     */
    public Empresa conId(long nuevoId) {
        return new Empresa(nuevoId, nombre, sector, web, fechaRegistro);
    }

    /**
     * Devuelve el nombre de la empresa, que es lo que se muestra en las listas desplegables.
     *
     * @return nombre de la empresa
     */
    @Override
    public String toString() {
        return nombre;
    }
}
