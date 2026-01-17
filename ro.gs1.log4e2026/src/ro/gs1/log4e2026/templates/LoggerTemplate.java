package ro.gs1.log4e2026.templates;

/**
 * Represents a logger template for a specific logging framework.
 */
public class LoggerTemplate {

    private final String id;
    private final String name;
    private final String loggerClass;
    private final String factoryClass;
    private final String factoryMethod;
    private final String declaration;
    private final String importStatement;

    public LoggerTemplate(String id, String name, String loggerClass, String factoryClass,
                          String factoryMethod, String declaration, String importStatement) {
        this.id = id;
        this.name = name;
        this.loggerClass = loggerClass;
        this.factoryClass = factoryClass;
        this.factoryMethod = factoryMethod;
        this.declaration = declaration;
        this.importStatement = importStatement;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLoggerClass() {
        return loggerClass;
    }

    public String getFactoryClass() {
        return factoryClass;
    }

    public String getFactoryMethod() {
        return factoryMethod;
    }

    public String getDeclaration() {
        return declaration;
    }

    public String getImportStatement() {
        return importStatement;
    }

    /**
     * Gets the logger type (simple class name).
     * @return the logger type name
     */
    public String getLoggerType() {
        if (loggerClass == null) {
            return "";
        }
        int lastDot = loggerClass.lastIndexOf('.');
        return lastDot >= 0 ? loggerClass.substring(lastDot + 1) : loggerClass;
    }

    /**
     * Gets the imports as an array.
     * @return array of import statements
     */
    public String[] getImports() {
        if (importStatement == null || importStatement.isEmpty()) {
            return new String[0];
        }
        return importStatement.split("\n");
    }

    public String getDeclaration(String className, String loggerName, boolean isStatic, boolean isFinal) {
        StringBuilder sb = new StringBuilder();
        sb.append("private ");
        if (isStatic) sb.append("static ");
        if (isFinal) sb.append("final ");
        sb.append(getLoggerClass()).append(" ").append(loggerName);
        sb.append(" = ").append(getFactoryClass()).append(".").append(getFactoryMethod());
        sb.append("(").append(className).append(".class);");
        return sb.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
