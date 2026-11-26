package tracker.repository;

import tracker.exception.TrackerException;
import tracker.model.Empresa;
import tracker.model.Estado;
import tracker.model.Oferta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla {@code oferta}. Las consultas de lectura unen cada oferta con su empresa. */
public class OfertaRepository {

    private static final String SELECT_CON_EMPRESA = """
            SELECT o.id, o.titulo, o.descripcion, o.estado, o.fechaPostulacion, o.enlace, o.salario, o.notas,
                   e.id AS empresa_id, e.nombre AS empresa_nombre, e.sector AS empresa_sector,
                   e.web AS empresa_web, e.fechaRegistro AS empresa_fecha
            FROM oferta o
            JOIN empresa e ON e.id = o.id_empresa
            """;

    /**
     * Guarda una oferta nueva.
     *
     * @param connection conexión a usar
     * @param oferta oferta a guardar; su empresa debe estar guardada
     * @return la oferta con el ID generado
     * @throws SQLException si falla la consulta
     */
    public Oferta insertar(Connection connection, Oferta oferta) throws SQLException {
        String sql = """
                INSERT INTO oferta (id_empresa, titulo, descripcion, estado, fechaPostulacion, enlace, salario, notas)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(statement, oferta);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return oferta.conId(keys.getLong(1));
            }
        }
    }

    /**
     * Modifica una oferta existente.
     *
     * @param connection conexión a usar
     * @param oferta oferta con los datos nuevos
     * @throws SQLException si falla la consulta
     * @throws TrackerException si la oferta no existe
     */
    public void actualizar(Connection connection, Oferta oferta) throws SQLException {
        String sql = """
                UPDATE oferta SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?,
                                  fechaPostulacion = ?, enlace = ?, salario = ?, notas = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            asignar(statement, oferta);
            statement.setLong(9, oferta.id());
            exigirFila(statement.executeUpdate(), oferta.id());
        }
    }

    /**
     * Cambia solo el estado de una oferta.
     *
     * @param connection conexión a usar
     * @param id identificador de la oferta
     * @param estado estado nuevo
     * @throws SQLException si falla la consulta
     * @throws TrackerException si la oferta no existe
     */
    public void cambiarEstado(Connection connection, long id, Estado estado) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE oferta SET estado = ? WHERE id = ?")) {
            statement.setString(1, estado.etiqueta());
            statement.setLong(2, id);
            exigirFila(statement.executeUpdate(), id);
        }
    }

    /**
     * Elimina una oferta junto con sus entrevistas.
     *
     * @param connection conexión a usar
     * @param id identificador de la oferta
     * @throws SQLException si falla la consulta
     * @throws TrackerException si la oferta no existe
     */
    public void eliminar(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM oferta WHERE id = ?")) {
            statement.setLong(1, id);
            exigirFila(statement.executeUpdate(), id);
        }
    }

    /**
     * Elimina todas las ofertas de una empresa, con sus entrevistas.
     *
     * @param connection conexión a usar
     * @param idEmpresa identificador de la empresa
     * @return número de ofertas eliminadas
     * @throws SQLException si falla la consulta
     */
    public int eliminarPorEmpresa(Connection connection, long idEmpresa) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM oferta WHERE id_empresa = ?")) {
            statement.setLong(1, idEmpresa);
            return statement.executeUpdate();
        }
    }

    /**
     * Cuenta las ofertas de una empresa.
     *
     * @param connection conexión a usar
     * @param idEmpresa identificador de la empresa
     * @return número de ofertas
     * @throws SQLException si falla la consulta
     */
    public int contarPorEmpresa(Connection connection, long idEmpresa) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM oferta WHERE id_empresa = ?")) {
            statement.setLong(1, idEmpresa);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    /**
     * Busca una oferta por su identificador, con los datos de su empresa.
     *
     * @param connection conexión a usar
     * @param id identificador de la oferta
     * @return la oferta, o vacío si no existe
     * @throws SQLException si falla la consulta
     */
    public Optional<Oferta> buscarPorId(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_CON_EMPRESA + "WHERE o.id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(leer(rows)) : Optional.empty();
            }
        }
    }

    /**
     * Lista las ofertas que cumplen todos los criterios del filtro, de la más reciente a la más antigua.
     * Una oferta sin salario no cumple ningún criterio de salario.
     *
     * @param connection conexión a usar
     * @param filtro criterios de búsqueda
     * @return ofertas encontradas
     * @throws SQLException si falla la consulta
     */
    public List<Oferta> listar(Connection connection, OfertaFiltro filtro) throws SQLException {
        List<String> condiciones = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();
        anadir(condiciones, parametros, "o.id_empresa = ?", filtro.idEmpresa());
        anadir(condiciones, parametros, "o.estado = ?", filtro.estado() == null ? null : filtro.estado().etiqueta());
        anadir(condiciones, parametros, "o.salario >= ?", filtro.salarioMin());
        anadir(condiciones, parametros, "o.salario <= ?", filtro.salarioMax());
        anadir(condiciones, parametros, "o.fechaPostulacion >= ?", filtro.desde());
        anadir(condiciones, parametros, "o.fechaPostulacion <= ?", filtro.hasta());

        String where = condiciones.isEmpty() ? "" : "WHERE " + String.join(" AND ", condiciones) + "\n";
        String sql = SELECT_CON_EMPRESA + where + "ORDER BY o.fechaPostulacion DESC, o.id DESC";

        List<Oferta> ofertas = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                statement.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    ofertas.add(leer(rows));
                }
            }
        }
        return ofertas;
    }

    private void anadir(List<String> condiciones, List<Object> parametros, String condicion, Object valor) {
        if (valor != null) {
            condiciones.add(condicion);
            parametros.add(valor);
        }
    }

    private void asignar(PreparedStatement statement, Oferta oferta) throws SQLException {
        statement.setLong(1, oferta.empresa().id());
        statement.setString(2, oferta.titulo());
        statement.setString(3, oferta.descripcion());
        statement.setString(4, oferta.estado().etiqueta());
        statement.setObject(5, oferta.fechaPostulacion());
        statement.setString(6, oferta.enlace());
        statement.setObject(7, oferta.salario());
        statement.setString(8, oferta.notas());
    }

    private Oferta leer(ResultSet rows) throws SQLException {
        Empresa empresa = new Empresa(
                rows.getLong("empresa_id"),
                rows.getString("empresa_nombre"),
                rows.getString("empresa_sector"),
                rows.getString("empresa_web"),
                rows.getObject("empresa_fecha", LocalDate.class));
        return new Oferta(
                rows.getLong("id"),
                empresa,
                rows.getString("titulo"),
                rows.getString("descripcion"),
                Estado.desdeTexto(rows.getString("estado")),
                rows.getObject("fechaPostulacion", LocalDate.class),
                rows.getString("enlace"),
                rows.getObject("salario", BigDecimal.class),
                rows.getString("notas"));
    }

    private void exigirFila(int filas, long id) {
        if (filas == 0) {
            throw new TrackerException("No existe la oferta con ID " + id);
        }
    }
}
