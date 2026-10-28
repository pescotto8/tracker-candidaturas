package tracker.service;

import tracker.exception.TrackerException;
import tracker.model.Candidatura;
import tracker.model.Estado;
import tracker.repository.CandidaturaRepository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lógica de negocio de las candidaturas. */
public class CandidaturaService {

    private final CandidaturaRepository repositorio;

    public CandidaturaService(CandidaturaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Lista las candidaturas; los filtros nulos se ignoran. La empresa se compara por contenido. */
    public List<Candidatura> listar(Estado estado, String empresa) {
        return repositorio.buscarTodas().stream()
                .filter(c -> estado == null || c.estado() == estado)
                .filter(c -> empresa == null || c.empresa().toLowerCase().contains(empresa.strip().toLowerCase()))
                .sorted(Comparator.comparingLong(Candidatura::id))
                .toList();
    }

    public Candidatura anadir(String titulo, String empresa, Estado estado, LocalDate fecha,
                              String enlace, String notas) {
        if (titulo == null || titulo.isBlank()) {
            throw new TrackerException("El título es obligatorio");
        }
        if (empresa == null || empresa.isBlank()) {
            throw new TrackerException("La empresa es obligatoria");
        }
        List<Candidatura> todas = repositorio.buscarTodas();
        Candidatura nueva = new Candidatura(siguienteId(todas), limpiar(titulo), limpiar(empresa),
                estado, fecha, limpiar(enlace), limpiar(notas));
        todas.add(nueva);
        repositorio.guardarTodas(todas);
        return nueva;
    }

    public Candidatura ver(long id) {
        return repositorio.buscarTodas().stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new TrackerException("No existe ninguna candidatura con ID " + id));
    }

    public void borrar(long id) {
        List<Candidatura> todas = repositorio.buscarTodas();
        if (!todas.removeIf(c -> c.id() == id)) {
            throw new TrackerException("No existe ninguna candidatura con ID " + id);
        }
        repositorio.guardarTodas(todas);
    }

    /** Importa un CSV añadiendo sus candidaturas con nuevos IDs; devuelve cuántas se importaron. */
    public int importar(Path origen) {
        List<Candidatura> importadas = CandidaturaRepository.leer(origen);
        List<Candidatura> todas = new ArrayList<>(repositorio.buscarTodas());
        long id = siguienteId(todas);
        for (Candidatura c : importadas) {
            todas.add(c.conId(id++));
        }
        repositorio.guardarTodas(todas);
        return importadas.size();
    }

    private static long siguienteId(List<Candidatura> candidaturas) {
        return candidaturas.stream().mapToLong(Candidatura::id).max().orElse(0) + 1;
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(';', ',').replace('\n', ' ').replace('\r', ' ').strip();
    }
}
