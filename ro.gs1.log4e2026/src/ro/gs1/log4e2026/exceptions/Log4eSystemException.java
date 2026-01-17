package ro.gs1.log4e2026.exceptions;

/**
 * System-level exception for Log4E plugin.
 * Ported from de.jayefem.log4e.exceptions.Log4ESystemException
 */
public class Log4eSystemException extends Log4eException {

    private static final long serialVersionUID = 1L;

    public Log4eSystemException() {
        super();
    }

    public Log4eSystemException(String message) {
        super(message);
    }

    public Log4eSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public Log4eSystemException(Throwable cause) {
        super(cause);
    }
}
