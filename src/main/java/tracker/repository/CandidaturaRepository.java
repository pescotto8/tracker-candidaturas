package tracker.repository;

import tracker.exception.TrackerException;
import tracker.model.Candidatura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Persistencia de candidaturas en un archivo de texto plano. */
public class CandidaturaRepository {

    private final Path archivo;

    public CandidaturaRepository(Path archivo) {
        this.archivo = archivo;
    }

    public List<Candidatura> buscarTodas() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        return leer(archivo);
    }

    public void guardarTodas(List<Candidatura> candidaturas) {
        List<String> lineas = new ArrayList<>();
        lineas.add(CandidaturaCodec.CABECERA);
        candidaturas.stream().map(CandidaturaCodec::formatear).forEach(lineas::add);
        try {
            Path directorio = archivo.toAbsolutePath().getParent();
            if (directorio != null) {
                Files.createDirectories(directorio);
            }
            Files.write(archivo, lineas);
        } catch (IOException e) {
            throw new TrackerException("No se pudo escribir en " + archivo + ": " + e.getMessage(), e);
        }
    }

    /** Lee un archivo de candidaturas con el mismo formato (cabecera opcional). */
    public static List<Candidatura> leer(Path origen) {
        if (!Files.isRegularFile(origen)) {
            throw new TrackerException("Archivo no encontrado: " + origen);
        }
        List<String> lineas;
        try (Stream<String> stream = Files.lines(origen)) {
            lineas = stream.toList();
        } catch (IOException | java.io.UncheckedIOException e) {
            throw new TrackerException("No se pudo leer " + origen + ": " + e.getMessage(), e);
        }
        List<Candidatura> resultado = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank() || (i == 0 && CandidaturaCodec.esCabecera(linea))) {
                continue;
            }
            resultado.add(CandidaturaCodec.parsear(linea, i + 1));
        }
        return resultado;
    }
}
