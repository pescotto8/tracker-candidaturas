package com.impulso.service;

import com.impulso.model.Empresa;
import com.impulso.model.Entrevista;
import com.impulso.model.Oferta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reglas de validación del prototipo (las muestra el panel "Validación de datos"). */
public final class Validador {

    private Validador() {
    }

    public static boolean urlValida(String url) {
        return url == null || url.isBlank() || url.matches("(?i)^https?://\\S+$");
    }

    public static List<String> validar(Oferta o) {
        List<String> e = new ArrayList<>();
        String t = o.getTitulo() == null ? "" : o.getTitulo().trim();
        if (t.length() < 3 || t.length() > 120) {
            e.add("El título debe tener entre 3 y 120 caracteres.");
        }
        if (o.getIdEmpresa() <= 0) {
            e.add("Selecciona una empresa registrada.");
        }
        if (o.getEstado() == null) {
            e.add("Selecciona un estado.");
        }
        if (o.getFechaPostulacion() == null) {
            e.add("La fecha de postulación es obligatoria.");
        } else if (o.getFechaPostulacion().isAfter(LocalDate.now())) {
            e.add("La fecha de postulación no puede ser posterior a hoy.");
        }
        if (o.getSalario() != null && o.getSalario() < 0) {
            e.add("El salario debe ser igual o mayor que 0.");
        }
        if (!urlValida(o.getEnlace())) {
            e.add("El enlace debe empezar por https:// o http://.");
        }
        return e;
    }

    public static List<String> validar(Empresa emp) {
        List<String> e = new ArrayList<>();
        String n = emp.getNombre() == null ? "" : emp.getNombre().trim();
        if (n.length() < 2 || n.length() > 100) {
            e.add("El nombre debe tener entre 2 y 100 caracteres.");
        }
        if (emp.getSector() == null || emp.getSector().isBlank()) {
            e.add("El sector es obligatorio.");
        }
        if (!urlValida(emp.getWeb())) {
            e.add("La web debe empezar por https:// o http://.");
        }
        if (emp.getFechaRegistro() == null) {
            e.add("La fecha de registro es obligatoria.");
        } else if (emp.getFechaRegistro().isAfter(LocalDate.now())) {
            e.add("La fecha de registro no puede ser posterior a hoy.");
        }
        return e;
    }

    public static List<String> validar(Entrevista en) {
        List<String> e = new ArrayList<>();
        if (en.getFecha() == null) {
            e.add("La fecha de la entrevista es obligatoria.");
        }
        if (en.getTipo() == null) {
            e.add("Selecciona el tipo de entrevista.");
        }
        if (en.getResultado() == null) {
            e.add("Selecciona el resultado.");
        }
        return e;
    }
}
