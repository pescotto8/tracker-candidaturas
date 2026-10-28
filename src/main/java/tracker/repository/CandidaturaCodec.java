package tracker.repository;

import tracker.exception.TrackerException;
import tracker.model.Candidatura;
import tracker.model.Estado;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Conversión entre {@link Candidatura} y su línea de texto separada por punto y coma. */
public final class CandidaturaCodec {

    public static final String CABECERA = "id;titulo;empresa;estado;fechaPostulacion;enlace;notas";
    public static final String SEPARADOR = ";";
    private static final int CAMPOS = 7;
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private CandidaturaCodec() {
    }

    public static boolean esCabecera(String linea) {
        return linea.strip().equalsIgnoreCase(CABECERA);
    }

    public static Candidatura parsear(String linea, int numeroLinea) {
        String[] campos = linea.split(SEPARADOR, -1);
        if (campos.length != CAMPOS) {
            throw new TrackerException("Línea %d inválida: se esperaban %d campos y hay %d"
                    .formatted(numeroLinea, CAMPOS, campos.length));
        }
        try {
            return new Candidatura(
                    Long.parseLong(campos[0].strip()),
                    campos[1].strip(),
                    campos[2].strip(),
                    Estado.desdeTexto(campos[3]),
                    parsearFecha(campos[4]),
                    campos[5].strip(),
                    campos[6].strip());
        } catch (NumberFormatException e) {
            throw new TrackerException("Línea %d inválida: el id '%s' no es numérico"
                    .formatted(numeroLinea, campos[0]), e);
        } catch (TrackerException e) {
            throw new TrackerException("Línea %d inválida: %s".formatted(numeroLinea, e.getMessage()), e);
        }
    }

    public static String formatear(Candidatura c) {
        return String.join(SEPARADOR,
                String.valueOf(c.id()),
                c.titulo(),
                c.empresa(),
                c.estado().etiqueta(),
                formatearFecha(c.fechaPostulacion()),
                c.enlace(),
                c.notas());
    }

    public static LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto.strip(), FECHA);
        } catch (DateTimeParseException e) {
            throw new TrackerException("Fecha no válida: '" + texto + "'. Formato esperado: dd/MM/yyyy", e);
        }
    }

    public static String formatearFecha(LocalDate fecha) {
        return FECHA.format(fecha);
    }
}
