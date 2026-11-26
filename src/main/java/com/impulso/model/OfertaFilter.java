package com.impulso.model;

import java.time.LocalDate;

/**
 * Filtros combinables del listado. Cualquier campo a null se ignora, de modo que
 * se admiten combinaciones arbitrarias (empresa + estado + salario, fecha + empresa...).
 */
public class OfertaFilter {
    public String texto;          // título, empresa o ID
    public Integer idEmpresa;
    public Estado estado;
    public LocalDate desde;
    public LocalDate hasta;
    public Double salarioMin;
    public Double salarioMax;

    public static OfertaFilter vacio() {
        return new OfertaFilter();
    }
}
