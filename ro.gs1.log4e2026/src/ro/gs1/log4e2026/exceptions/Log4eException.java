package ro.gs1.log4e2026.exceptions;

/**
 * Base exception for Log4E plugin.
 * Ported from de.jayefem.exceptions.JayBaseException
 */
public class Log4eException extends Exception {

    private static final long serialVersionUID = 1L;

    public Log4eException() {
        super();
    }

    public Log4eException(String message) {
        super(message);
    }

    public Log4eException(String message, Throwable cause) {
        super(message, cause);
    }

    public Log4eException(Throwable cause) {
        super(cause);
    }
}
