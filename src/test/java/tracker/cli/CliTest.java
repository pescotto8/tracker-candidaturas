package tracker.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracker.repository.CandidaturaRepository;
import tracker.service.CandidaturaService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {

    @TempDir
    Path directorio;

    private ByteArrayOutputStream salida;
    private ByteArrayOutputStream errores;
    private Cli cli;

    @BeforeEach
    void preparar() {
        salida = new ByteArrayOutputStream();
        errores = new ByteArrayOutputStream();
        CandidaturaService servicio = new CandidaturaService(
                new CandidaturaRepository(directorio.resolve("datos.txt")));
        cli = new Cli(servicio, new PrintStream(salida, true, StandardCharsets.UTF_8),
                new PrintStream(errores, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return errores.toString(StandardCharsets.UTF_8);
    }

    @Test
    void flujoCompletoAnadirListarVerBorrar() {
        assertEquals(0, cli.ejecutar(new String[]{"-añadir", "--titulo", "Dev Java", "--empresa", "Indra",
                "--estado", "pendiente", "--fecha", "15/09/2026"}));
        assertTrue(out().contains("ID 1"));

        assertEquals(0, cli.ejecutar(new String[]{"-listar", "--estado", "pendiente", "--empresa", "Indra"}));
        assertTrue(out().contains("Dev Java"));

        assertEquals(0, cli.ejecutar(new String[]{"-ver", "1"}));
        assertTrue(out().contains("15/09/2026"));

        assertEquals(0, cli.ejecutar(new String[]{"-borrar", "1"}));
        assertEquals(1, cli.ejecutar(new String[]{"-ver", "1"}));
        assertTrue(err().contains("No existe ninguna candidatura con ID 1"));
    }

    @Test
    void importarCargaElCsv() throws IOException {
        Path csv = directorio.resolve("entrada.csv");
        Files.writeString(csv, """
                id;titulo;empresa;estado;fechaPostulacion;enlace;notas
                1;Dev;Indra;pendiente;01/10/2026;;
                2;QA;Cabify;aceptado;02/10/2026;;ok
                """);

        assertEquals(0, cli.ejecutar(new String[]{"-importar", csv.toString()}));
        assertTrue(out().contains("Importadas 2"));
    }

    @Test
    void erroresMuestranMensajeClaro() {
        assertEquals(1, cli.ejecutar(new String[]{}));
        assertEquals(1, cli.ejecutar(new String[]{"-añadir", "--titulo", "Dev"}));
        assertTrue(err().contains("--empresa"));
        assertEquals(1, cli.ejecutar(new String[]{"-importar", "noexiste.csv"}));
        assertTrue(err().contains("Archivo no encontrado"));
        assertEquals(1, cli.ejecutar(new String[]{"-ver", "abc"}));
        assertEquals(1, cli.ejecutar(new String[]{"-añadir", "--titulo", "X", "--empresa", "Y", "--fecha", "31/02/2026"}));
        assertTrue(err().contains("Fecha no válida"));
        assertEquals(1, cli.ejecutar(new String[]{"-listar", "--estado", "raro"}));
        assertEquals(1, cli.ejecutar(new String[]{"-nada"}));
    }
}
