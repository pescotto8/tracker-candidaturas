package com.impulso.service;

import com.impulso.db.DataAccessException;
import com.impulso.db.Database;
import com.impulso.model.Empresa;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.repository.EmpresaRepository;
import com.impulso.repository.OfertaRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importa el CSV de candidaturas de la versión 1.0 (separador ';', fechas dd/MM/yyyy):
 * id;titulo;empresa;estado;fechaPostulacion;enlace;notas
 * <p>
 * El ID del archivo se ignora. Las empresas se reutilizan por nombre o se crean con el sector
 * "Sin especificar". Las candidaturas que ya existen (misma empresa, título y fecha) se omiten.
 * Todo el archivo se importa en UNA transacción: si una fila falla se hace rollback y la base de datos
 * queda exactamente como estaba.
 */
public class CsvImporter {

    /**
     * Resumen de una importación.
     *
     * @param empresasNuevas empresas creadas
     * @param ofertas candidaturas guardadas
     * @param omitidas candidaturas que ya existían
     */
    public record Resultado(int empresasNuevas, int ofertas, int omitidas) {
    }

    record Fila(int linea, String empresa, Oferta oferta) {
    }

    static final String SECTOR_POR_DEFECTO = "Sin especificar";
    private static final String CABECERA = "id;titulo;empresa;estado;fechaPostulacion;enlace;notas";
    private static final int CAMPOS = 7;
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final EmpresaRepository empresas = new EmpresaRepository();
    private final OfertaRepository ofertas = new OfertaRepository();

    public Resultado importar(Path csv) {
        List<Fila> filas = leer(csv);
        if (filas.isEmpty()) {
            throw new DataAccessException("El CSV no contiene datos.");
        }
        return Database.transaction(c -> {
            Map<String, Integer> idPorNombre = new HashMap<>();
            int nuevas = 0;
            int importadas = 0;
            int omitidas = 0;
            for (Fila fila : filas) {
                String nombre = fila.empresa();
                Integer idEmpresa = idPorNombre.get(nombre.toLowerCase());
                if (idEmpresa == null) {
                    var existente = empresas.buscarPorNombre(c, nombre);
                    if (existente.isPresent()) {
                        idEmpresa = existente.get().getId();
                    } else {
                        Empresa e = new Empresa(0, nombre, SECTOR_POR_DEFECTO, null, LocalDate.now());
                        comprobar(fila.linea(), Validador.validar(e));
                        idEmpresa = empresas.insertar(c, e);
                        nuevas++;
                    }
                    idPorNombre.put(nombre.toLowerCase(), idEmpresa);
                }
                Oferta o = fila.oferta();
                o.setIdEmpresa(idEmpresa);
                comprobar(fila.linea(), Validador.validar(o));
                if (ofertas.existe(c, idEmpresa, o.getTitulo(), o.getFechaPostulacion())) {
                    omitidas++;
                } else {
                    ofertas.insertar(c, o);
                    importadas++;
                }
            }
            return new Resultado(nuevas, importadas, omitidas);
        });
    }

    private static void comprobar(int linea, List<String> errores) {
        if (!errores.isEmpty()) {
            throw new DataAccessException(mensaje(linea, String.join(" ", errores)));
        }
    }

    private static String mensaje(int linea, String detalle) {
        return "Línea " + linea + " del CSV: " + detalle + " No se ha importado nada.";
    }

    private static List<Fila> leer(Path csv) {
        List<String> lineas;
        try {
            lineas = Files.readAllLines(csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataAccessException("No se pudo leer " + csv + ": " + e.getMessage());
        }
        List<Fila> filas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank() || (i == 0 && linea.strip().equalsIgnoreCase(CABECERA))) {
                continue;
            }
            try {
                filas.add(parsear(i + 1, partir(linea)));
            } catch (IllegalArgumentException e) {
                throw new DataAccessException(mensaje(i + 1, e.getMessage()));
            }
        }
        return filas;
    }

    static Fila parsear(int linea, String[] campos) {
        if (campos.length != CAMPOS) {
            throw new IllegalArgumentException("se esperaban " + CAMPOS + " campos y hay " + campos.length + ".");
        }
        String empresa = campos[2].trim();
        Oferta o = new Oferta();
        o.setTitulo(campos[1].trim());
        o.setEstado(parsearEstado(campos[3]));
        o.setFechaPostulacion(parsearFecha(campos[4]));
        o.setEnlace(campos[5].isBlank() ? null : campos[5].trim());
        o.setNotas(campos[6].isBlank() ? null : campos[6].trim());
        return new Fila(linea, empresa, o);
    }

    private static Estado parsearEstado(String texto) {
        try {
            return Estado.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("estado no válido: '" + texto.trim()
                    + "'. Valores posibles: pendiente, entrevista, aceptado, rechazado.");
        }
    }

    private static LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("fecha no válida: '" + texto.trim() + "'. Formato esperado: dd/MM/yyyy.");
        }
    }

    /** Divide una línea por ';' respetando comillas dobles ("" = comilla escapada). */
    static String[] partir(String line) {
        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean comillas = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (comillas && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    comillas = !comillas;
                }
            } else if (ch == ';' && !comillas) {
                campos.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        campos.add(sb.toString());
        return campos.toArray(new String[0]);
    }
}
