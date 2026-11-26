package com.impulso.service;

import com.impulso.db.DataAccessException;
import com.impulso.db.Database;
import com.impulso.model.Empresa;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.repository.EmpresaRepository;
import com.impulso.repository.OfertaRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importa el CSV inicial. Cabecera esperada (separador ';'):
 * empresa;sector;web;fechaRegistro;titulo;descripcion;estado;fechaPostulacion;enlace;salario;notas
 * <p>
 * Todo el fichero se importa en UNA transacción: si una fila falla se hace rollback
 * y la base de datos queda exactamente como estaba.
 */
public class CsvImporter {

    public record Resultado(int empresasNuevas, int ofertas) {
    }

    private final EmpresaRepository empresas = new EmpresaRepository();
    private final OfertaRepository ofertas = new OfertaRepository();

    public Resultado importar(Path csv) {
        List<String[]> filas = leer(csv);
        if (filas.size() < 2) {
            throw new DataAccessException("El CSV no contiene datos.");
        }
        return Database.transaction(c -> {
            Map<String, Integer> idPorNombre = new HashMap<>();
            int nuevas = 0;
            int importadas = 0;
            for (int i = 1; i < filas.size(); i++) {
                String[] f = filas.get(i);
                try {
                    if (f.length < 10) {
                        throw new IllegalArgumentException("faltan columnas");
                    }
                    String nombre = f[0].trim();
                    Integer idEmpresa = idPorNombre.get(nombre.toLowerCase());
                    if (idEmpresa == null) {
                        var existente = empresas.buscarPorNombre(c, nombre);
                        if (existente.isPresent()) {
                            idEmpresa = existente.get().getId();
                        } else {
                            Empresa e = new Empresa(0, nombre, f[1].trim(), f[2].trim(), LocalDate.parse(f[3].trim()));
                            idEmpresa = empresas.insertar(c, e);
                            nuevas++;
                        }
                        idPorNombre.put(nombre.toLowerCase(), idEmpresa);
                    }
                    Oferta o = new Oferta();
                    o.setIdEmpresa(idEmpresa);
                    o.setTitulo(f[4].trim());
                    o.setDescripcion(f[5]);
                    o.setEstado(Estado.valueOf(f[6].trim().toUpperCase()));
                    o.setFechaPostulacion(LocalDate.parse(f[7].trim()));
                    o.setEnlace(f[8]);
                    o.setSalario(f[9].isBlank() ? null : Double.valueOf(f[9].trim()));
                    o.setNotas(f.length > 10 ? f[10] : null);
                    List<String> errores = Validador.validar(o);
                    if (!errores.isEmpty()) {
                        throw new IllegalArgumentException(String.join(" ", errores));
                    }
                    ofertas.insertar(c, o);
                    importadas++;
                } catch (RuntimeException ex) {
                    throw new DataAccessException("Fila " + (i + 1) + " del CSV: " + ex.getMessage()
                            + ". No se ha importado nada.");
                }
            }
            return new Resultado(nuevas, importadas);
        });
    }

    private static List<String[]> leer(Path csv) {
        List<String[]> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    out.add(partir(line));
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("No se pudo leer " + csv + ": " + e.getMessage());
        }
        return out;
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
