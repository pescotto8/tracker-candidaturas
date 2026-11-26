package tracker.repository;

import tracker.exception.TrackerException;
import tracker.model.Entrevista;
import tracker.model.ResultadoEntrevista;
import tracker.model.TipoEntrevista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla {@code entrevista}. Todos los métodos usan la conexión que se les pasa. */
public class EntrevistaRepository {

    private static final String COLUMNAS = "id, id_oferta, fecha, hora, tipo, notas, resultado";

    /**
     * Guarda una entrevista nueva.
     *
     * @param connection conexión a usar
     * @param entrevista entrevista a guardar; su oferta debe estar guardada
     * @return la entrevista con el ID generado
     * @throws SQLException si falla la consulta
     */
    public Entrevista insertar(Connection connection, Entrevista entrevista) throws SQLException {
        String sql = "INSERT INTO entrevista (id_oferta, fecha, hora, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(statement, entrevista);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return entrevista.conId(keys.getLong(1));
            }
        }
    }

    /**
     * Modifica una entrevista existente.
     *
     * @param connection conexión a usar
     * @param entrevista entrevista con los datos nuevos
     * @throws SQLException si falla la consulta
     * @throws TrackerException si la entrevista no existe
     */
    public void actualizar(Connection connection, Entrevista entrevista) throws SQLException {
        String sql = """
                UPDATE entrevista SET id_oferta = ?, fecha = ?, hora = ?, tipo = ?, notas = ?, resultado = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            asignar(statement, entrevista);
            statement.setLong(7, entrevista.id());
            exigirFila(statement.executeUpdate(), entrevista.id());
        }
    }

    /**
     * Elimina una entrevista.
     *
     * @param connection conexión a usar
     * @param id identificador de la entrevista
     * @throws SQLException si falla la consulta
     * @throws TrackerException si la entrevista no existe
     */
    public void eliminar(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM entrevista WHERE id = ?")) {
            statement.setLong(1, id);
            exigirFila(statement.executeUpdate(), id);
        }
    }

    /**
     * Busca una entrevista por su identificador.
     *
     * @param connection conexión a usar
     * @param id identificador de la entrevista
     * @return la entrevista, o vacío si no existe
     * @throws SQLException si falla la consulta
     */
    public Optional<Entrevista> buscarPorId(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + COLUMNAS + " FROM entrevista WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(leer(rows)) : Optional.empty();
            }
        }
    }

    /**
     * Lista las entrevistas de una oferta, de la más reciente a la más antigua.
     *
     * @param connection conexión a usar
     * @param idOferta identificador de la oferta
     * @return entrevistas de la oferta
     * @throws SQLException si falla la consulta
     */
    public List<Entrevista> listarPorOferta(Connection connection, long idOferta) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM entrevista WHERE id_oferta = ? ORDER BY fecha DESC, hora DESC, id DESC";
        List<Entrevista> entrevistas = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idOferta);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    entrevistas.add(leer(rows));
                }
            }
        }
        return entrevistas;
    }

    private void asignar(PreparedStatement statement, Entrevista entrevista) throws SQLException {
        statement.setLong(1, entrevista.idOferta());
        statement.setObject(2, entrevista.fecha());
        statement.setObject(3, entrevista.hora());
        statement.setString(4, entrevista.tipo().etiqueta());
        statement.setString(5, entrevista.notas());
        statement.setString(6, entrevista.resultado().etiqueta());
    }

    private Entrevista leer(ResultSet rows) throws SQLException {
        return new Entrevista(
                rows.getLong("id"),
                rows.getLong("id_oferta"),
                rows.getObject("fecha", LocalDate.class),
                rows.getObject("hora", LocalTime.class),
                TipoEntrevista.desdeTexto(rows.getString("tipo")),
                rows.getString("notas"),
                ResultadoEntrevista.desdeTexto(rows.getString("resultado")));
    }

    private void exigirFila(int filas, long id) {
        if (filas == 0) {
            throw new TrackerException("No existe la entrevista con ID " + id);
        }
    }
}
