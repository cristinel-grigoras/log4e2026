package ro.gs1.log4e2026.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.preferences.ProjectPreferences;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Base class for logging operations that manipulate the AST.
 */
public class LoggingOperation {

    private final OperationContext context;
    private LoggerTemplate template;
    private String loggerName;

    public LoggingOperation(OperationContext context) {
        this.context = context;
        initializeFromPreferences();
    }

    private void initializeFromPreferences() {
        // Get project-aware preferences
        ProjectPreferences prefs = null;
        if (context.getCompilationUnit() != null) {
            prefs = Log4e2026Plugin.getProjectPreferences(
                    context.getCompilationUnit().getJavaProject().getProject());
        } else {
            prefs = Log4e2026Plugin.getProjectPreferences(null);
        }

        String framework = prefs.getLoggingFramework();
        this.template = LoggerTemplates.getTemplate(framework);
        if (this.template == null) {
            this.template = LoggerTemplates.getSLF4J();
        }
        this.loggerName = prefs.getLoggerName();
        if (this.loggerName == null || this.loggerName.isEmpty()) {
            this.loggerName = "logger";
        }
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
     */
    public Statement createEntryLogStatement(AST ast, MethodDeclaration method) {
        String methodName = ASTUtil.getName(method);
        String message = methodName + "() - start";
        return createLogStatement(ast, LogLevel.DEBUG, message);
    }

    /**
     * Creates a log statement for method exit.
     */
    public Statement createExitLogStatement(AST ast, MethodDeclaration method) {
        String methodName = ASTUtil.getName(method);
        String message = methodName + "() - end";
        return createLogStatement(ast, LogLevel.DEBUG, message);
    }

    /**
     * Creates a log statement for a catch block.
     */
    public Statement createCatchLogStatement(AST ast, CatchClause catchClause, MethodDeclaration method) {
        String methodName = ASTUtil.getName(method);
        String exceptionName = ASTUtil.getExceptionName(catchClause);
        String message = methodName + "() - " + exceptionName;
        return createErrorLogStatement(ast, message, exceptionName);
    }

    /**
     * Creates a log statement for a variable.
     */
    public Statement createVariableLogStatement(AST ast, String variableName, String methodName) {
        String message = methodName + "() - " + variableName + "={}";
        return createLogStatementWithArg(ast, LogLevel.DEBUG, message, variableName);
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
}
