package com.impulso.db;

import com.impulso.util.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Acceso a conexiones JDBC y utilidades transaccionales. */
public final class Database {

    @FunctionalInterface
    public interface SqlWork<T> {
        T run(Connection c) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlVoidWork {
        void run(Connection c) throws SQLException;
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.get("db.url", "jdbc:mysql://localhost:3306/impulso"),
                Config.get("db.user", "impulso"),
                Config.get("db.password", ""));
    }

    /** Ejecuta una consulta sin transacción explícita (auto-commit). */
    public static <T> T query(SqlWork<T> work) {
        try (Connection c = getConnection()) {
            return work.run(c);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /** Ejecuta varias operaciones atómicamente: commit si todo va bien, rollback si algo falla. */
    public static <T> T transaction(SqlWork<T> work) {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static void transactionVoid(SqlVoidWork work) {
        transaction(c -> {
            work.run(c);
            return null;
        });
    }
}
