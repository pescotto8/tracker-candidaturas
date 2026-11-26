package tracker.repository;

import tracker.model.Estado;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Criterios para filtrar ofertas. Los criterios {@code null} se ignoran y los demás se combinan con AND.
 *
 * @param idEmpresa identificador de la empresa
 * @param estado estado de la oferta
 * @param salarioMin salario mínimo, incluido
 * @param salarioMax salario máximo, incluido
 * @param desde fecha de postulación mínima, incluida
 * @param hasta fecha de postulación máxima, incluida
 */
public record OfertaFiltro(
        Long idEmpresa,
        Estado estado,
        BigDecimal salarioMin,
        BigDecimal salarioMax,
        LocalDate desde,
        LocalDate hasta) {

    /**
     * Crea un filtro sin criterios, que devuelve todas las ofertas.
     *
     * @return filtro vacío
     */
    public static OfertaFiltro sinFiltros() {
        return new OfertaFiltro(null, null, null, null, null, null);
    }
}
