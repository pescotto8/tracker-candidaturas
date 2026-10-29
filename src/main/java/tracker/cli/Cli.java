package tracker.cli;

import tracker.exception.TrackerException;
import tracker.model.Candidatura;
import tracker.model.Estado;
import tracker.repository.CandidaturaCodec;
import tracker.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Interfaz de línea de comandos: ejecuta una operación a partir de los argumentos de main. */
public class Cli {

    private static final String USO = """
            Uso:
              -listar [--estado X] [--empresa Y]
              -añadir --titulo X --empresa Y [--estado Z] [--fecha dd/MM/yyyy] [--enlace URL] [--notas TEXTO]
              -ver <id>
              -borrar <id>
              -importar <fichero.csv>""";

    private final CandidaturaService servicio;
    private final PrintStream out;
    private final PrintStream err;

    /**
     * Crea la interfaz de línea de comandos.
     *
     * @param servicio lógica de negocio
     * @param out salida de resultados
     * @param err salida de errores
     */
    public Cli(CandidaturaService servicio, PrintStream out, PrintStream err) {
        this.servicio = servicio;
        this.out = out;
        this.err = err;
    }

    /**
     * Ejecuta la operación indicada por los argumentos.
     *
     * @param args operación seguida de sus argumentos
     * @return código de salida: 0 si fue correcto, 1 si hubo un error
     */
    public int ejecutar(String[] args) {
        try {
            if (args.length == 0) {
                throw new TrackerException("No se indicó ninguna operación.\n" + USO);
            }
            String comando = args[0].replaceFirst("^-+", "").toLowerCase();
            String[] resto = Arrays.copyOfRange(args, 1, args.length);
            switch (comando) {
                case "listar" -> listar(resto);
                case "añadir", "anadir" -> anadir(resto);
                case "ver" -> ver(resto);
                case "borrar" -> borrar(resto);
                case "importar" -> importar(resto);
                default -> throw new TrackerException("Operación desconocida: " + args[0] + "\n" + USO);
            }
            return 0;
        } catch (TrackerException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Muestra las candidaturas en una tabla, con filtros opcionales {@code --estado} y {@code --empresa}.
     *
     * @param args argumentos que siguen al nombre de la operación
     */
    private void listar(String[] args) {
        Map<String, String> opciones = opciones(args, Set.of("estado", "empresa"));
        Estado estado = opciones.containsKey("estado") ? Estado.desdeTexto(opciones.get("estado")) : null;
        List<Candidatura> resultado = servicio.listar(estado, opciones.get("empresa"));
        if (resultado.isEmpty()) {
            out.println("No hay candidaturas que mostrar.");
            return;
        }
        String formato = "%-4s %-32s %-20s %-11s %-10s%n";
        out.printf(formato, "ID", "TÍTULO", "EMPRESA", "ESTADO", "FECHA");
        for (Candidatura c : resultado) {
            out.printf(formato, c.id(), recortar(c.titulo(), 32), recortar(c.empresa(), 20),
                    c.estado().etiqueta(), CandidaturaCodec.formatearFecha(c.fechaPostulacion()));
        }
        out.println("Total: " + resultado.size());
    }

    /**
     * Añade una candidatura a partir de las opciones indicadas.
     *
     * @param args argumentos que siguen al nombre de la operación
     */
    private void anadir(String[] args) {
        Map<String, String> opciones = opciones(args, Set.of("titulo", "empresa", "estado", "fecha", "enlace", "notas"));
        for (String obligatoria : List.of("titulo", "empresa")) {
            if (!opciones.containsKey(obligatoria)) {
                throw new TrackerException("Falta el argumento obligatorio --" + obligatoria);
            }
        }
        Estado estado = opciones.containsKey("estado") ? Estado.desdeTexto(opciones.get("estado")) : Estado.PENDIENTE;
        LocalDate fecha = opciones.containsKey("fecha")
                ? CandidaturaCodec.parsearFecha(opciones.get("fecha"))
                : LocalDate.now();
        Candidatura nueva = servicio.anadir(opciones.get("titulo"), opciones.get("empresa"), estado, fecha,
                opciones.get("enlace"), opciones.get("notas"));
        out.println("Candidatura añadida con ID " + nueva.id());
    }

    /**
     * Muestra todos los detalles de una candidatura.
     *
     * @param args argumentos que siguen al nombre de la operación
     */
    private void ver(String[] args) {
        Candidatura c = servicio.ver(argumentoId(args, "ver"));
        out.println("ID:          " + c.id());
        out.println("Título:      " + c.titulo());
        out.println("Empresa:     " + c.empresa());
        out.println("Estado:      " + c.estado().etiqueta());
        out.println("Postulación: " + CandidaturaCodec.formatearFecha(c.fechaPostulacion()));
        out.println("Enlace:      " + valorOGuion(c.enlace()));
        out.println("Notas:       " + valorOGuion(c.notas()));
    }

    /**
     * Elimina una candidatura por ID.
     *
     * @param args argumentos que siguen al nombre de la operación
     */
    private void borrar(String[] args) {
        long id = argumentoId(args, "borrar");
        servicio.borrar(id);
        out.println("Candidatura " + id + " eliminada");
    }

    /**
     * Importa las candidaturas de otro archivo CSV.
     *
     * @param args argumentos que siguen al nombre de la operación
     */
    private void importar(String[] args) {
        if (args.length != 1) {
            throw new TrackerException("Uso: -importar <fichero.csv>");
        }
        int total = servicio.importar(Path.of(args[0]));
        out.println("Importadas " + total + " candidaturas");
    }

    /**
     * Convierte los argumentos {@code --clave valor} en un mapa y valida que sean permitidos.
     *
     * @param args argumentos de la operación
     * @param permitidas nombres de opción aceptados, sin los guiones
     * @return opciones y sus valores
     */
    private static Map<String, String> opciones(String[] args, Set<String> permitidas) {
        Map<String, String> opciones = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                throw new TrackerException("Argumento inesperado: " + args[i]);
            }
            String clave = args[i].substring(2);
            if (!permitidas.contains(clave)) {
                throw new TrackerException("Opción desconocida: " + args[i]);
            }
            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                throw new TrackerException("Falta el valor de --" + clave);
            }
            opciones.put(clave, args[++i]);
        }
        return opciones;
    }

    /**
     * Lee el argumento de un comando que espera un único ID numérico.
     *
     * @param args argumentos de la operación
     * @param comando nombre de la operación, usado en el mensaje de uso
     * @return ID leído
     */
    private static long argumentoId(String[] args, String comando) {
        if (args.length != 1) {
            throw new TrackerException("Uso: -" + comando + " <id>");
        }
        try {
            return Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new TrackerException("El ID debe ser un número: '" + args[0] + "'", e);
        }
    }

    /**
     * Acorta un texto con puntos suspensivos si supera el máximo.
     *
     * @param texto texto original
     * @param maximo longitud máxima
     * @return texto acortado
     */
    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }

    /**
     * Devuelve el texto o un guion si está vacío.
     *
     * @param texto texto original
     * @return texto o {@code "-"}
     */
    private static String valorOGuion(String texto) {
        return texto.isBlank() ? "-" : texto;
    }
}
