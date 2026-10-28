package tracker.exception;

/** Error de uso o de datos que se muestra al usuario como mensaje de consola. */
public class TrackerException extends RuntimeException {

    public TrackerException(String message) {
        super(message);
    }

    public TrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
