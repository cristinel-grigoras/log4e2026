package ro.gs1.log4e2026.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pool to collect warnings during operations.
 * Ported from de.jayefem.log4e.exceptions.WarningsPool
 */
public class WarningsPool {

    private static final ThreadLocal<WarningsPool> INSTANCE = ThreadLocal.withInitial(WarningsPool::new);

    private final List<Log4eWarning> warnings = new ArrayList<>();

    private WarningsPool() {
    }

    public static WarningsPool getInstance() {
        return INSTANCE.get();
    }

    public void addWarning(Log4eWarning warning) {
        if (warning != null) {
            warnings.add(warning);
        }
    }

    public void addWarning(String message) {
        addWarning(new Log4eWarning(message));
    }

    public void addWarning(String message, String resourceName) {
        addWarning(new Log4eWarning(message, resourceName));
    }

    public void addWarning(String message, String resourceName, int lineNumber) {
        addWarning(new Log4eWarning(message, resourceName, lineNumber));
    }

    public List<Log4eWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public void clear() {
        warnings.clear();
    }

    public void reset() {
        clear();
    }
}
