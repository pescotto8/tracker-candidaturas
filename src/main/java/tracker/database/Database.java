package tracker.database;

import tracker.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Punto de acceso a la base de datos: abre conexiones y gestiona las transacciones. */
public class Database {

    private final DatabaseConfig config;

    /**
     * Crea el acceso a la base de datos.
     *
     * @param config datos de conexión
     */
    public Database(DatabaseConfig config) {
        this.config = config;
    }

    /**
     * Ejecuta un trabajo con una conexión en modo autocommit, que se cierra al terminar.
     *
     * @param work trabajo a ejecutar
     * @param <T> tipo del resultado
     * @return resultado del trabajo
     * @throws DataAccessException si falla la conexión o alguna operación SQL
     */
    public <T> T execute(SqlWork<T> work) {
        try (Connection connection = open()) {
            return work.run(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error en la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta un trabajo dentro de una transacción: se confirma si termina bien
     * y se deshace por completo si lanza una excepción.
     *
     * @param work trabajo a ejecutar
     * @param <T> tipo del resultado
     * @return resultado del trabajo
     * @throws DataAccessException si falla la conexión o alguna operación SQL
     */
    public <T> T transaction(SqlWork<T> work) {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error en la base de datos: " + e.getMessage(), e);
        }
    }

    private Connection open() {
        try {
            return DriverManager.getConnection(config.url(), config.user(), config.password());
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo conectar con la base de datos. "
                    + "Comprueba que el servidor está en marcha y la configuración: " + e.getMessage(), e);
        }
    }

    private static void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
