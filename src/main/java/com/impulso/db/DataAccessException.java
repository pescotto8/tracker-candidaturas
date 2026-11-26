package com.impulso.db;

/** Envuelve SQLException para que la UI no tenga que declararla. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
