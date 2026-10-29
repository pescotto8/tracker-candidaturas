package tracker.exception;

/** Error de uso o de datos que se muestra al usuario como mensaje de consola. */
public class TrackerException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje legible para el usuario.
     *
     * @param message descripción del error
     */
    public TrackerException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje y la causa original.
     *
     * @param message descripción del error
     * @param cause excepción que originó el error
     */
    public TrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
