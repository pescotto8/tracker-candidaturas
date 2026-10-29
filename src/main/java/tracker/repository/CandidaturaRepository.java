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

    /**
     * Crea el repositorio sobre un archivo de texto.
     *
     * @param archivo ruta del archivo de datos; se crea al guardar si no existe
     */
    public CandidaturaRepository(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Lee todas las candidaturas del archivo.
     *
     * @return lista modificable; vacía si el archivo no existe
     * @throws TrackerException si el archivo no se puede leer o tiene líneas inválidas
     */
    public List<Candidatura> buscarTodas() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        return leer(archivo);
    }

    /**
     * Sustituye el contenido del archivo por las candidaturas indicadas.
     *
     * @param candidaturas candidaturas a guardar
     * @throws TrackerException si no se puede escribir en el archivo
     */
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

    /**
     * Lee un archivo de candidaturas con el mismo formato (cabecera opcional).
     *
     * @param origen archivo a leer
     * @return candidaturas leídas
     * @throws TrackerException si el archivo no existe, no se puede leer o tiene líneas inválidas
     */
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
