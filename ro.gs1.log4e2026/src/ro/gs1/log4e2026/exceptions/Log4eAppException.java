package ro.gs1.log4e2026.exceptions;

/**
 * Application-level exception for Log4E plugin.
 * Ported from de.jayefem.log4e.exceptions.Log4EAppException
 */
public class Log4eAppException extends Log4eException {

    private static final long serialVersionUID = 1L;

    public Log4eAppException() {
        super();
    }

    public Log4eAppException(String message) {
        super(message);
    }

    public Log4eAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public Log4eAppException(Throwable cause) {
        super(cause);
    }
}
