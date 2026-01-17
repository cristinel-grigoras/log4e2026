package ro.gs1.log4e2026.exceptions;

import org.eclipse.core.runtime.IStatus;

/**
 * Warning status for Log4E.
 * Ported from de.jayefem.log4e.exceptions.Log4EWarning
 */
public class Log4eWarning extends Log4eStatus {

    private final String resourceName;
    private final int lineNumber;

    public Log4eWarning(String message) {
        this(message, null, -1);
    }

    public Log4eWarning(String message, String resourceName) {
        this(message, resourceName, -1);
    }

    public Log4eWarning(String message, String resourceName, int lineNumber) {
        super(IStatus.WARNING, message);
        this.resourceName = resourceName;
        this.lineNumber = lineNumber;
    }

    public String getResourceName() {
        return resourceName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Warning: ").append(getMessage());
        if (resourceName != null) {
            sb.append(" [").append(resourceName);
            if (lineNumber >= 0) {
                sb.append(":").append(lineNumber);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
