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

    /**
     * Crea el servicio.
     *
     * @param repositorio origen y destino de los datos
     */
    public CandidaturaService(CandidaturaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Lista las candidaturas ordenadas por ID; los filtros nulos se ignoran.
     * La empresa se compara por contenido, sin distinguir mayúsculas.
     *
     * @param estado estado a filtrar, o {@code null}
     * @param empresa texto que debe contener el nombre de la empresa, o {@code null}
     * @return candidaturas que cumplen los filtros
     */
    public List<Candidatura> listar(Estado estado, String empresa) {
        return repositorio.buscarTodas().stream()
                .filter(c -> estado == null || c.estado() == estado)
                .filter(c -> empresa == null || c.empresa().toLowerCase().contains(empresa.strip().toLowerCase()))
                .sorted(Comparator.comparingLong(Candidatura::id))
                .toList();
    }

    /**
     * Añade una candidatura con el siguiente ID libre.
     * Los puntos y coma y los saltos de línea de los textos se sustituyen para no romper el formato del archivo.
     *
     * @param titulo título de la oferta (obligatorio)
     * @param empresa nombre de la empresa (obligatorio)
     * @param estado estado inicial
     * @param fecha fecha de postulación
     * @param enlace URL de la oferta; puede ser nulo
     * @param notas notas personales; pueden ser nulas
     * @return candidatura guardada
     * @throws TrackerException si falta el título o la empresa
     */
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

    /**
     * Busca una candidatura por ID.
     *
     * @param id identificador
     * @return candidatura encontrada
     * @throws TrackerException si no existe
     */
    public Candidatura ver(long id) {
        return repositorio.buscarTodas().stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new TrackerException("No existe ninguna candidatura con ID " + id));
    }

    /**
     * Elimina una candidatura.
     *
     * @param id identificador
     * @throws TrackerException si no existe
     */
    public void borrar(long id) {
        List<Candidatura> todas = repositorio.buscarTodas();
        if (!todas.removeIf(c -> c.id() == id)) {
            throw new TrackerException("No existe ninguna candidatura con ID " + id);
        }
        repositorio.guardarTodas(todas);
    }

    /**
     * Importa un CSV añadiendo sus candidaturas con nuevos IDs.
     *
     * @param origen archivo CSV a importar
     * @return número de candidaturas importadas
     * @throws TrackerException si el archivo no existe o tiene líneas inválidas
     */
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

    /**
     * Calcula el siguiente ID libre (el mayor existente más uno).
     */
    private static long siguienteId(List<Candidatura> candidaturas) {
        return candidaturas.stream().mapToLong(Candidatura::id).max().orElse(0) + 1;
    }

    /**
     * Normaliza un texto para guardarlo en una línea del archivo.
     */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(';', ',').replace('\n', ' ').replace('\r', ' ').strip();
    }
}
