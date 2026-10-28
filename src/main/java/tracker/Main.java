package tracker;

import tracker.cli.Cli;
import tracker.repository.CandidaturaRepository;
import tracker.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Punto de entrada. El archivo de datos se puede cambiar con la variable de entorno TRACKER_DATA. */
public final class Main {

    private static final String ARCHIVO_POR_DEFECTO = "data/candidaturas.txt";

    private Main() {
    }

    public static void main(String[] args) {
        String ruta = System.getenv().getOrDefault("TRACKER_DATA", ARCHIVO_POR_DEFECTO);
        CandidaturaService servicio = new CandidaturaService(new CandidaturaRepository(Path.of(ruta)));
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        System.exit(new Cli(servicio, out, err).ejecutar(args));
    }
}
