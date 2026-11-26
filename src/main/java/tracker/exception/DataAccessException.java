package tracker.exception;

/** Error al acceder a la base de datos, con un mensaje legible para el usuario. */
public class DataAccessException extends TrackerException {

    /**
     * Crea la excepción con un mensaje y la causa original.
     *
     * @param message descripción del error
     * @param cause excepción SQL que originó el error
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
