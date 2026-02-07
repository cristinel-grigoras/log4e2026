package ro.gs1.log4e2026.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.preference.IPreferenceStore;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.preferences.PreferenceKeys;
import ro.gs1.log4e2026.preferences.ProjectPreferences;
import ro.gs1.log4e2026.settings.PositionCatchSettings;
import ro.gs1.log4e2026.settings.PositionEndSettings;
import ro.gs1.log4e2026.settings.PositionStartSettings;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Base class for logging operations that manipulate the AST.
 * Enhanced with position settings integration, conditional wrapping,
 * and parameter logging support.
 */
public class LoggingOperation {

    private final OperationContext context;
    private LoggerTemplate template;
    private String loggerName;
    private IPreferenceStore prefs;

    // Position settings
    private PositionStartSettings startSettings;
    private PositionEndSettings endSettings;
    private PositionCatchSettings catchSettings;

    // Conditional wrapping preference
    private boolean useConditionalWrapping;

    // Format settings
    private String delimiter;
    private String variablePlaceholder;

    public LoggingOperation(OperationContext context) {
        this.context = context;
        initializeFromPreferences();
    }

    private void initializeFromPreferences() {
        // Get project-aware preferences
        ProjectPreferences projectPrefs = null;
        if (context.getCompilationUnit() != null) {
            projectPrefs = Log4e2026Plugin.getProjectPreferences(
                    context.getCompilationUnit().getJavaProject().getProject());
        } else {
            projectPrefs = Log4e2026Plugin.getProjectPreferences(null);
        }

        String framework = projectPrefs.getLoggingFramework();
        this.template = LoggerTemplates.getTemplate(framework);
        if (this.template == null) {
            this.template = LoggerTemplates.getSLF4J();
        }
        this.loggerName = projectPrefs.getLoggerName();
        if (this.loggerName == null || this.loggerName.isEmpty()) {
            this.loggerName = "logger";
        }

        // Initialize position settings
        this.startSettings = new PositionStartSettings();
        this.endSettings = new PositionEndSettings();
        this.catchSettings = new PositionCatchSettings();

        // Get preference store for additional settings
        this.prefs = Log4e2026Plugin.getPreferences();
        this.useConditionalWrapping = prefs.getBoolean(PreferenceKeys.ENABLED_BRACES);
        this.delimiter = prefs.getString(PreferenceKeys.FORMAT_DELIMITER);
        if (this.delimiter == null || this.delimiter.isEmpty()) {
            this.delimiter = " - ";
        }
        this.variablePlaceholder = prefs.getString(PreferenceKeys.LOGGER_FORMAT_VARIABLE_PLACEHOLDER);
        if (this.variablePlaceholder == null || this.variablePlaceholder.isEmpty()) {
            this.variablePlaceholder = "{}";
        }
    }

    // ========== Position Settings Accessors ==========

    public PositionStartSettings getStartSettings() {
        return startSettings;
    }

    public PositionEndSettings getEndSettings() {
        return endSettings;
    }

    public PositionCatchSettings getCatchSettings() {
        return catchSettings;
    }

    /**
     * Gets the log level for method start position from preferences.
     */
    public LogLevel getStartLogLevel() {
        String level = prefs.getString(PreferenceKeys.POS_START + PreferenceKeys.POS_ATT_LEVEL);
        LogLevel logLevel = LogLevel.fromName(level);
        return logLevel != null ? logLevel : LogLevel.DEBUG;
    }

    /**
     * Gets the log level for method end position from preferences.
     */
    public LogLevel getEndLogLevel() {
        String level = prefs.getString(PreferenceKeys.POS_END + PreferenceKeys.POS_ATT_LEVEL);
        LogLevel logLevel = LogLevel.fromName(level);
        return logLevel != null ? logLevel : LogLevel.DEBUG;
    }

    /**
     * Gets the log level for catch blocks from preferences.
     */
    public LogLevel getCatchLogLevel() {
        String level = prefs.getString(PreferenceKeys.POS_CATCH + PreferenceKeys.POS_ATT_LEVEL);
        LogLevel logLevel = LogLevel.fromName(level);
        return logLevel != null ? logLevel : LogLevel.ERROR;
    }

    /**
     * Checks if logging at start position is enabled.
     */
    public boolean isStartLoggingEnabled() {
        return !prefs.getBoolean(PreferenceKeys.POS_START + PreferenceKeys.POS_ATT_DISABLE_ADD_LOGGING);
    }

    /**
     * Checks if logging at end position is enabled.
     */
    public boolean isEndLoggingEnabled() {
        return !prefs.getBoolean(PreferenceKeys.POS_END + PreferenceKeys.POS_ATT_DISABLE_ADD_LOGGING);
    }

    /**
     * Checks if logging in catch blocks is enabled.
     */
    public boolean isCatchLoggingEnabled() {
        return !prefs.getBoolean(PreferenceKeys.POS_CATCH + PreferenceKeys.POS_ATT_DISABLE_ADD_LOGGING);
    }

    /**
     * Returns whether parameter values should be included in entry logs.
     */
    public boolean includeParameterValues() {
        return prefs.getBoolean(PreferenceKeys.POS_START + PreferenceKeys.POS_ATT_PARAMVALUES);
    }

    /**
     * Returns whether parameter names should be included in entry logs.
     */
    public boolean includeParameterNames() {
        return prefs.getBoolean(PreferenceKeys.POS_START + PreferenceKeys.POS_ATT_PARAMNAMES);
    }

    /**
     * Returns whether return value should be included in exit logs.
     */
    public boolean includeReturnValue() {
        return prefs.getBoolean(PreferenceKeys.POS_END + PreferenceKeys.POS_ATT_RETURN_VALUE);
    }

    // ========== Method Skip Checks ==========

    /**
     * Checks if a method should be skipped based on position settings.
     */
    public boolean shouldSkipMethod(MethodDeclaration method, String position) {
        if (method == null) {
            return true;
        }

        String prefix = position;

        // Check skip settings
        if (prefs.getBoolean(prefix + PreferenceKeys.POS_ATT_SKIP_GETTER) && ASTUtil.isGetter(method)) {
            return true;
        }
        if (prefs.getBoolean(prefix + PreferenceKeys.POS_ATT_SKIP_SETTER) && ASTUtil.isSetter(method)) {
            return true;
        }
        if (prefs.getBoolean(prefix + PreferenceKeys.POS_ATT_SKIP_TO_STRING) && ASTUtil.isToStringMethod(method)) {
            return true;
        }
        if (prefs.getBoolean(prefix + PreferenceKeys.POS_ATT_SKIP_CONSTRUCTOR) && method.isConstructor()) {
            return true;
        }
        if (prefs.getBoolean(prefix + PreferenceKeys.POS_ATT_SKIP_EMPTY_METHODS)) {
            Block body = method.getBody();
            if (body == null || body.statements().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if method should be skipped for start logging.
     */
    public boolean shouldSkipForStart(MethodDeclaration method) {
        return shouldSkipMethod(method, PreferenceKeys.POS_START);
    }

    /**
     * Checks if method should be skipped for end logging.
     */
    public boolean shouldSkipForEnd(MethodDeclaration method) {
        return shouldSkipMethod(method, PreferenceKeys.POS_END);
    }

    public OperationContext getContext() {
        return context;
    }

    public LoggerTemplate getTemplate() {
        return template;
    }

    public String getLoggerName() {
        return loggerName;
    }

    /**
     * Creates a log statement for method entry.
     * Uses position settings for log level and includes parameters if configured.
     */
    public Statement createEntryLogStatement(AST ast, MethodDeclaration method) {
        String methodName = ASTUtil.getName(method);
        LogLevel level = getStartLogLevel();
        String customMsg = prefs.getString(PreferenceKeys.POS_START + PreferenceKeys.POS_ATT_MSG);
        if (customMsg == null || customMsg.isEmpty()) {
            customMsg = "start";
        }

        // Build message with optional parameters
        StringBuilder message = new StringBuilder();
        message.append(methodName).append("()").append(delimiter).append(customMsg);

        // Check if parameters should be included
        if (includeParameterNames() || includeParameterValues()) {
            List<String> paramInfo = extractParameterInfo(method);
            if (!paramInfo.isEmpty()) {
                message.append(delimiter);
                if (includeParameterValues()) {
                    // Use placeholders for values: param1={}, param2={}
                    message.append(String.join(", ", paramInfo));
                    return createLogStatementWithParams(ast, level, message.toString(), method);
                } else {
                    // Just names: param1, param2
                    message.append(String.join(", ", getParameterNames(method)));
                }
            }
        }

        Statement logStmt = createLogStatement(ast, level, message.toString());
        return wrapWithConditionalIfNeeded(ast, level, logStmt);
    }

    /**
     * Creates a log statement for method exit.
     * Uses position settings for log level.
     */
    public Statement createExitLogStatement(AST ast, MethodDeclaration method) {
        String methodName = ASTUtil.getName(method);
        LogLevel level = getEndLogLevel();
        String customMsg = prefs.getString(PreferenceKeys.POS_END + PreferenceKeys.POS_ATT_MSG);
        if (customMsg == null || customMsg.isEmpty()) {
            customMsg = "end";
        }

        String message = methodName + "()" + delimiter + customMsg;
        Statement logStmt = createLogStatement(ast, level, message);
        return wrapWithConditionalIfNeeded(ast, level, logStmt);
    }

    /**
     * Creates a log statement for method exit with return value.
     * Uses position settings for log level and return value inclusion.
     */
    public Statement createExitLogStatementWithReturn(AST ast, MethodDeclaration method, String returnVarName) {
        String methodName = ASTUtil.getName(method);
        LogLevel level = getEndLogLevel();
        String customMsg = prefs.getString(PreferenceKeys.POS_END + PreferenceKeys.POS_ATT_MSG);
        if (customMsg == null || customMsg.isEmpty()) {
            customMsg = "end";
        }

        if (includeReturnValue() && returnVarName != null && !returnVarName.isEmpty()) {
            String message = methodName + "()" + delimiter + customMsg + delimiter + "returning=" + variablePlaceholder;
            Statement logStmt = createLogStatementWithArg(ast, level, message, returnVarName);
            return wrapWithConditionalIfNeeded(ast, level, logStmt);
        } else {
            return createExitLogStatement(ast, method);
        }
    }

    /**
     * Creates a log statement for a catch block.
     * Uses position settings for log level.
     */
    public Statement createCatchLogStatement(AST ast, CatchClause catchClause, MethodDeclaration method) {
        String methodName = ASTUtil.getName(method);
        String exceptionName = ASTUtil.getExceptionName(catchClause);
        LogLevel level = getCatchLogLevel();
        String customMsg = prefs.getString(PreferenceKeys.POS_CATCH + PreferenceKeys.POS_ATT_MSG);
        if (customMsg == null || customMsg.isEmpty()) {
            customMsg = "exception";
        }

        String message = methodName + "()" + delimiter + customMsg;
        // Error level statements typically don't need conditional wrapping
        return createErrorLogStatement(ast, message, exceptionName);
    }

    /**
     * Creates a log statement for a variable.
     */
    public Statement createVariableLogStatement(AST ast, String variableName, String methodName) {
        String message = methodName + "()" + delimiter + variableName + "=" + variablePlaceholder;
        Statement logStmt = createLogStatementWithArg(ast, LogLevel.DEBUG, message, variableName);
        return wrapWithConditionalIfNeeded(ast, LogLevel.DEBUG, logStmt);
    }

    // ========== Parameter Logging Support ==========

    /**
     * Extracts parameter info for logging.
     * Returns list of "paramName={}" strings for placeholder format.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractParameterInfo(MethodDeclaration method) {
        List<String> params = new ArrayList<>();
        List<SingleVariableDeclaration> parameters = method.parameters();
        for (SingleVariableDeclaration param : parameters) {
            String name = param.getName().getIdentifier();
            params.add(name + "=" + variablePlaceholder);
        }
        return params;
    }

    /**
     * Extracts just parameter names from method.
     */
    @SuppressWarnings("unchecked")
    private List<String> getParameterNames(MethodDeclaration method) {
        List<String> names = new ArrayList<>();
        List<SingleVariableDeclaration> parameters = method.parameters();
        for (SingleVariableDeclaration param : parameters) {
            names.add(param.getName().getIdentifier());
        }
        return names;
    }

    /**
     * Creates a log statement with method parameters as arguments.
     */
    @SuppressWarnings("unchecked")
    public Statement createLogStatementWithParams(AST ast, LogLevel level, String message, MethodDeclaration method) {
        MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setExpression(ast.newSimpleName(loggerName));
        invocation.setName(ast.newSimpleName(level.getMethodName()));

        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue(message);
        invocation.arguments().add(literal);

        // Add each parameter as an argument
        List<SingleVariableDeclaration> parameters = method.parameters();
        for (SingleVariableDeclaration param : parameters) {
            invocation.arguments().add(ast.newSimpleName(param.getName().getIdentifier()));
        }

        Statement logStmt = ast.newExpressionStatement(invocation);
        return wrapWithConditionalIfNeeded(ast, level, logStmt);
    }

    // ========== Conditional Wrapping ==========

    /**
     * Wraps a log statement with if(logger.isXxxEnabled()) check if configured.
     */
    @SuppressWarnings("unchecked")
    public Statement wrapWithConditionalIfNeeded(AST ast, LogLevel level, Statement logStatement) {
        if (!useConditionalWrapping) {
            return logStatement;
        }

        // Don't wrap error/fatal level - they're usually always enabled
        if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
            return logStatement;
        }

        // Get the is-enabled method name
        String isEnabledMethod = getIsEnabledMethodName(level);
        if (isEnabledMethod == null || isEnabledMethod.isEmpty()) {
            return logStatement;
        }

        // Create: if (logger.isDebugEnabled()) { ... }
        IfStatement ifStmt = ast.newIfStatement();

        // Create condition: logger.isDebugEnabled()
        MethodInvocation condition = ast.newMethodInvocation();
        condition.setExpression(ast.newSimpleName(loggerName));
        condition.setName(ast.newSimpleName(isEnabledMethod));
        ifStmt.setExpression(condition);

        // Create body block with the log statement
        Block thenBlock = ast.newBlock();
        thenBlock.statements().add(logStatement);
        ifStmt.setThenStatement(thenBlock);

        return ifStmt;
    }

    /**
     * Gets the isXxxEnabled method name for a log level.
     */
    private String getIsEnabledMethodName(LogLevel level) {
        return switch (level) {
            case TRACE -> "isTraceEnabled";
            case DEBUG -> "isDebugEnabled";
            case INFO -> "isInfoEnabled";
            case WARN -> "isWarnEnabled";
            case ERROR -> "isErrorEnabled";
            case FINEST -> "isLoggable"; // JUL uses isLoggable(Level)
            case FINER -> "isLoggable";
            case FATAL -> null; // No check for fatal
        };
    }

    /**
     * Checks if a statement is wrapped in a conditional.
     */
    public boolean isConditionallyWrapped(Statement statement) {
        if (!(statement instanceof IfStatement)) {
            return false;
        }
        IfStatement ifStmt = (IfStatement) statement;
        if (!(ifStmt.getExpression() instanceof MethodInvocation)) {
            return false;
        }
        MethodInvocation condition = (MethodInvocation) ifStmt.getExpression();
        String methodName = condition.getName().getIdentifier();
        return methodName.startsWith("is") && methodName.endsWith("Enabled");
    }

    /**
     * Extracts the log statement from a conditionally wrapped statement.
     */
    @SuppressWarnings("unchecked")
    public Statement unwrapConditional(Statement statement) {
        if (!isConditionallyWrapped(statement)) {
            return statement;
        }
        IfStatement ifStmt = (IfStatement) statement;
        Statement thenStmt = ifStmt.getThenStatement();
        if (thenStmt instanceof Block) {
            Block block = (Block) thenStmt;
            List<Statement> statements = block.statements();
            if (!statements.isEmpty()) {
                return statements.get(0);
            }
        }
        return thenStmt;
    }

    /**
     * Creates a basic log statement.
     */
    @SuppressWarnings("unchecked")
    public Statement createLogStatement(AST ast, LogLevel level, String message) {
        MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setExpression(ast.newSimpleName(loggerName));
        invocation.setName(ast.newSimpleName(level.getMethodName()));

        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue(message);
        invocation.arguments().add(literal);

        return ast.newExpressionStatement(invocation);
    }

    /**
     * Creates a log statement with an argument.
     */
    @SuppressWarnings("unchecked")
    public Statement createLogStatementWithArg(AST ast, LogLevel level, String message, String argName) {
        MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setExpression(ast.newSimpleName(loggerName));
        invocation.setName(ast.newSimpleName(level.getMethodName()));

        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue(message);
        invocation.arguments().add(literal);
        invocation.arguments().add(ast.newSimpleName(argName));

        return ast.newExpressionStatement(invocation);
    }

    /**
     * Creates an error log statement with exception.
     */
    @SuppressWarnings("unchecked")
    public Statement createErrorLogStatement(AST ast, String message, String exceptionName) {
        MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setExpression(ast.newSimpleName(loggerName));
        invocation.setName(ast.newSimpleName("error"));

        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue(message);
        invocation.arguments().add(literal);
        invocation.arguments().add(ast.newSimpleName(exceptionName));

        return ast.newExpressionStatement(invocation);
    }

    /**
     * Inserts a statement at the beginning of a block.
     */
    @SuppressWarnings("unchecked")
    public void insertAtBlockStart(ASTRewrite rewrite, Block block, Statement statement) {
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        listRewrite.insertFirst(statement, null);
    }

    /**
     * Inserts a statement at the end of a block.
     */
    @SuppressWarnings("unchecked")
    public void insertAtBlockEnd(ASTRewrite rewrite, Block block, Statement statement) {
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        listRewrite.insertLast(statement, null);
    }

    /**
     * Inserts a statement before a return statement.
     */
    @SuppressWarnings("unchecked")
    public void insertBeforeReturn(ASTRewrite rewrite, Block block, Statement logStatement, Statement returnStatement) {
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        listRewrite.insertBefore(logStatement, returnStatement, null);
    }

    /**
     * Checks if a logger is already declared in the type.
     */
    public boolean isLoggerDeclared(TypeDeclaration type) {
        return ASTUtil.findVariableInType(loggerName, type) != null;
    }

    /**
     * Checks if a statement is a log statement.
     */
    public boolean isLogStatement(Statement statement) {
        if (!(statement instanceof ExpressionStatement)) {
            return false;
        }
        ExpressionStatement exprStmt = (ExpressionStatement) statement;
        if (!(exprStmt.getExpression() instanceof MethodInvocation)) {
            return false;
        }
        MethodInvocation invocation = (MethodInvocation) exprStmt.getExpression();
        String expr = invocation.getExpression() != null ? invocation.getExpression().toString() : "";
        return expr.equals(loggerName);
    }

    /**
     * Removes all log statements from a block.
     */
    @SuppressWarnings("unchecked")
    public void removeLogStatements(ASTRewrite rewrite, Block block) {
        if (block == null) {
            return;
        }
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        for (Object obj : block.statements()) {
            Statement stmt = (Statement) obj;
            if (isLogStatement(stmt)) {
                listRewrite.remove(stmt, null);
            }
        }
    }

    /**
     * Checks if a method invocation is System.out.println or System.err.println.
     */
    public boolean isSystemPrintln(MethodInvocation invocation) {
        if (invocation == null) {
            return false;
        }
        String methodName = ASTUtil.getName(invocation);
        if (!"println".equals(methodName) && !"print".equals(methodName)) {
            return false;
        }
        String expr = invocation.getExpression() != null ? invocation.getExpression().toString() : "";
        return "System.out".equals(expr) || "System.err".equals(expr);
    }

    /**
     * Replaces a System.out/err.println with a log statement.
     */
    @SuppressWarnings("unchecked")
    public Statement replaceSystemPrintln(AST ast, MethodInvocation println, LogLevel level) {
        List<?> args = println.arguments();
        String message = args.isEmpty() ? "" : args.get(0).toString();

        // Remove quotes if present
        if (message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1);
        }

        return createLogStatement(ast, level, message);
    }

    // ========== Advanced Removal with Import Cleanup ==========

    /**
     * Checks if a statement is a log statement (including conditionally wrapped ones).
     */
    public boolean isLogStatementOrWrapped(Statement statement) {
        if (isLogStatement(statement)) {
            return true;
        }
        if (isConditionallyWrapped(statement)) {
            Statement unwrapped = unwrapConditional(statement);
            return isLogStatement(unwrapped);
        }
        return false;
    }

    /**
     * Removes all log statements from a block, including conditionally wrapped ones.
     */
    @SuppressWarnings("unchecked")
    public void removeLogStatementsAdvanced(ASTRewrite rewrite, Block block) {
        if (block == null) {
            return;
        }
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        for (Object obj : block.statements()) {
            Statement stmt = (Statement) obj;
            if (isLogStatementOrWrapped(stmt)) {
                listRewrite.remove(stmt, null);
            }
        }
    }

    /**
     * Checks if the logger is still used in the compilation unit.
     * Used to determine if logger declaration and imports can be removed.
     */
    public boolean isLoggerUsedInCompilationUnit(CompilationUnit cu) {
        if (cu == null) {
            return false;
        }

        final boolean[] found = {false};
        cu.accept(new org.eclipse.jdt.core.dom.ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getExpression() != null &&
                    node.getExpression().toString().equals(loggerName)) {
                    found[0] = true;
                    return false; // Stop visiting
                }
                return true;
            }
        });

        return found[0];
    }

    /**
     * Gets the list of imports that should be removed when logger is removed.
     * Based on the current template's imports.
     */
    public List<String> getLoggerImports() {
        List<String> imports = new ArrayList<>();
        if (template != null) {
            String[] templateImports = template.getImports();
            for (String imp : templateImports) {
                imports.add(imp);
            }
        }
        return imports;
    }

    /**
     * Removes logger-related imports from compilation unit.
     */
    @SuppressWarnings("unchecked")
    public void removeLoggerImports(ASTRewrite rewrite, CompilationUnit cu) {
        List<String> loggerImports = getLoggerImports();
        if (loggerImports.isEmpty()) {
            return;
        }

        ListRewrite importRewrite = rewrite.getListRewrite(cu,
                CompilationUnit.IMPORTS_PROPERTY);

        for (Object obj : cu.imports()) {
            org.eclipse.jdt.core.dom.ImportDeclaration importDecl =
                    (org.eclipse.jdt.core.dom.ImportDeclaration) obj;
            String importName = importDecl.getName().getFullyQualifiedName();
            if (loggerImports.contains(importName)) {
                importRewrite.remove(importDecl, null);
            }
        }
    }

    /**
     * Removes the logger field declaration from a type.
     */
    @SuppressWarnings("unchecked")
    public void removeLoggerDeclaration(ASTRewrite rewrite, TypeDeclaration type) {
        if (type == null) {
            return;
        }

        org.eclipse.jdt.core.dom.FieldDeclaration[] fields = type.getFields();
        for (org.eclipse.jdt.core.dom.FieldDeclaration field : fields) {
            List<org.eclipse.jdt.core.dom.VariableDeclarationFragment> fragments =
                    field.fragments();
            for (org.eclipse.jdt.core.dom.VariableDeclarationFragment fragment : fragments) {
                if (fragment.getName().getIdentifier().equals(loggerName)) {
                    rewrite.remove(field, null);
                    return;
                }
            }
        }
    }

    /**
     * Performs complete logger removal from a type:
     * 1. Removes all log statements from all methods
     * 2. Removes logger field declaration
     * 3. Removes logger imports if logger is no longer used anywhere
     */
    @SuppressWarnings("unchecked")
    public void removeLoggerComplete(ASTRewrite rewrite, TypeDeclaration type, CompilationUnit cu) {
        // Remove log statements from all methods
        org.eclipse.jdt.core.dom.MethodDeclaration[] methods = type.getMethods();
        for (org.eclipse.jdt.core.dom.MethodDeclaration method : methods) {
            Block body = method.getBody();
            if (body != null) {
                removeLogStatementsAdvanced(rewrite, body);
            }
        }

        // Remove logger declaration
        removeLoggerDeclaration(rewrite, type);

        // Check if logger is still used elsewhere (e.g., inner classes)
        // For now, we'll remove imports - could be enhanced to check more thoroughly
        removeLoggerImports(rewrite, cu);
    }

    // ========== Utility Methods ==========

    /**
     * Returns whether conditional wrapping is enabled.
     */
    public boolean isConditionalWrappingEnabled() {
        return useConditionalWrapping;
    }

    /**
     * Sets whether conditional wrapping is enabled (for testing).
     */
    public void setConditionalWrappingEnabled(boolean enabled) {
        this.useConditionalWrapping = enabled;
    }

    /**
     * Gets the delimiter used in log messages.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Gets the variable placeholder used in log messages (e.g., "{}").
     */
    public String getVariablePlaceholder() {
        return variablePlaceholder;
    }
}
