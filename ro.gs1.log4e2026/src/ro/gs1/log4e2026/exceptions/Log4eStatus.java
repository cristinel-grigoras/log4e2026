package ro.gs1.log4e2026.exceptions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Eclipse Status wrapper for Log4E.
 * Ported from de.jayefem.log4e.exceptions.Log4EStatus
 */
public class Log4eStatus extends Status {

    public Log4eStatus(int severity, String message) {
        super(severity, Log4e2026Plugin.PLUGIN_ID, message);
    }

    public Log4eStatus(int severity, String message, Throwable exception) {
        super(severity, Log4e2026Plugin.PLUGIN_ID, message, exception);
    }

    public static IStatus createError(String message) {
        return new Log4eStatus(IStatus.ERROR, message);
    }

    public static IStatus createError(String message, Throwable exception) {
        return new Log4eStatus(IStatus.ERROR, message, exception);
    }

    public static IStatus createWarning(String message) {
        return new Log4eStatus(IStatus.WARNING, message);
    }

    public static IStatus createInfo(String message) {
        return new Log4eStatus(IStatus.INFO, message);
    }

    public static IStatus createOK() {
        return new Log4eStatus(IStatus.OK, "OK");
    }
}
