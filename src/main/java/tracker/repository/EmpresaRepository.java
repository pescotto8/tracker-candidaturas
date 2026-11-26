package tracker.repository;

import tracker.exception.TrackerException;
import tracker.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla {@code empresa}. Todos los métodos usan la conexión que se les pasa. */
public class EmpresaRepository {

    private static final String COLUMNAS = "id, nombre, sector, web, fechaRegistro";

    /**
     * Guarda una empresa nueva.
     *
     * @param connection conexión a usar
     * @param empresa empresa a guardar
     * @return la empresa con el ID generado
     * @throws SQLException si falla la consulta
     */
    public Empresa insertar(Connection connection, Empresa empresa) throws SQLException {
        String sql = "INSERT INTO empresa (nombre, sector, web, fechaRegistro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, empresa.nombre());
            statement.setString(2, empresa.sector());
            statement.setString(3, empresa.web());
            statement.setObject(4, empresa.fechaRegistro());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return empresa.conId(keys.getLong(1));
            }
        }
    }

    /**
     * Modifica una empresa existente.
     *
     * @param connection conexión a usar
     * @param empresa empresa con los datos nuevos
     * @throws SQLException si falla la consulta
     * @throws TrackerException si la empresa no existe
     */
    public void actualizar(Connection connection, Empresa empresa) throws SQLException {
        String sql = "UPDATE empresa SET nombre = ?, sector = ?, web = ?, fechaRegistro = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, empresa.nombre());
            statement.setString(2, empresa.sector());
            statement.setString(3, empresa.web());
            statement.setObject(4, empresa.fechaRegistro());
            statement.setLong(5, empresa.id());
            exigirFila(statement.executeUpdate(), empresa.id());
        }
    }

    /**
     * Elimina una empresa. Falla si todavía tiene ofertas.
     *
     * @param connection conexión a usar
     * @param id identificador de la empresa
     * @throws SQLException si falla la consulta o la empresa tiene ofertas
     * @throws TrackerException si la empresa no existe
     */
    public void eliminar(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM empresa WHERE id = ?")) {
            statement.setLong(1, id);
            exigirFila(statement.executeUpdate(), id);
        }
    }

    /**
     * Busca una empresa por su identificador.
     *
     * @param connection conexión a usar
     * @param id identificador de la empresa
     * @return la empresa, o vacío si no existe
     * @throws SQLException si falla la consulta
     */
    public Optional<Empresa> buscarPorId(Connection connection, long id) throws SQLException {
        return buscarUna(connection, "SELECT " + COLUMNAS + " FROM empresa WHERE id = ?", id);
    }

    /**
     * Busca una empresa por su nombre, sin distinguir mayúsculas.
     *
     * @param connection conexión a usar
     * @param nombre nombre de la empresa
     * @return la empresa, o vacío si no existe
     * @throws SQLException si falla la consulta
     */
    public Optional<Empresa> buscarPorNombre(Connection connection, String nombre) throws SQLException {
        return buscarUna(connection, "SELECT " + COLUMNAS + " FROM empresa WHERE LOWER(nombre) = LOWER(?)", nombre);
    }

    /**
     * Lista todas las empresas ordenadas por nombre.
     *
     * @param connection conexión a usar
     * @return empresas guardadas
     * @throws SQLException si falla la consulta
     */
    public List<Empresa> listar(Connection connection) throws SQLException {
        List<Empresa> empresas = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT " + COLUMNAS + " FROM empresa ORDER BY nombre")) {
            while (rows.next()) {
                empresas.add(leer(rows));
            }
        }
        return empresas;
    }

    private Optional<Empresa> buscarUna(Connection connection, String sql, Object parametro) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parametro);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(leer(rows)) : Optional.empty();
            }
        }
    }

    private Empresa leer(ResultSet rows) throws SQLException {
        return new Empresa(
                rows.getLong("id"),
                rows.getString("nombre"),
                rows.getString("sector"),
                rows.getString("web"),
                rows.getObject("fechaRegistro", LocalDate.class));
    }

    private void exigirFila(int filas, long id) {
        if (filas == 0) {
            throw new TrackerException("No existe la empresa con ID " + id);
        }
    }
}
