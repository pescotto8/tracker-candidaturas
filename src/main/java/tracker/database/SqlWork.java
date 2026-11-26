package tracker.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Trabajo que se ejecuta con una conexión abierta.
 *
 * @param <T> tipo del resultado
 */
@FunctionalInterface
public interface SqlWork<T> {

    /**
     * Ejecuta el trabajo.
     *
     * @param connection conexión abierta, que cierra quien la proporciona
     * @return resultado del trabajo
     * @throws SQLException si falla alguna operación SQL
     */
    T run(Connection connection) throws SQLException;
}
