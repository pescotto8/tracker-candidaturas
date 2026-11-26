package tracker.model;

import tracker.exception.TrackerException;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Conversión de texto a valores de enumeraciones, sin distinguir mayúsculas ni tildes. */
final class Etiquetas {

    private Etiquetas() {
    }

    /**
     * Busca el valor de una enumeración cuyo nombre coincide con el texto.
     *
     * @param tipo enumeración en la que buscar
     * @param texto texto a convertir
     * @param campo nombre del dato, usado en el mensaje de error
     * @param <E> tipo de la enumeración
     * @return valor encontrado
     * @throws TrackerException si el texto no corresponde a ningún valor
     */
    static <E extends Enum<E>> E parsear(Class<E> tipo, String texto, String campo) {
        String buscado = sinTildes(texto == null ? "" : texto.strip());
        for (E valor : tipo.getEnumConstants()) {
            if (valor.name().equalsIgnoreCase(buscado)) {
                return valor;
            }
        }
        String validos = Arrays.stream(tipo.getEnumConstants())
                .map(valor -> valor.name().toLowerCase())
                .collect(Collectors.joining(", "));
        throw new TrackerException(campo + " no válido: '" + texto + "'. Valores posibles: " + validos);
    }

    private static String sinTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
