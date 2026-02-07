package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;
import ro.gs1.log4e2026.preferences.PreferenceConstants;

/**
 * Handler for adding logging to the entire current method.
 * Adds entry, exit, and catch block logging based on position settings.
 * Supports:
 * - Position-specific log levels (from preferences)
 * - Conditional wrapping (if logger.isXxxEnabled())
 * - Parameter logging for entry
 * - Return value logging for exit
 * - Skip settings for getters/setters/etc.
 * Keyboard shortcut: Ctrl+Alt+I
 */
public class LogMethodHandler extends BaseLogHandler {

    @Override
    protected String getWizardPreferenceKey() {
        return PreferenceConstants.P_WIZARD_INSERT_METHOD;
    }

    @Override
    protected String getWizardTitle() {
        return "Insert Log in Method";
    }

    @Override
    protected void executeOperation(OperationContext context, String selectedText) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();

        if (method == null || method.getBody() == null) {
            logWarning("No method found at cursor position");
            return;
        }

        LoggingOperation operation = createOperation(context);

        // Check if method should be skipped (getter/setter/toString/etc.)
        if (operation.shouldSkipForStart(method) && operation.shouldSkipForEnd(method)) {
            logWarning("Method '" + ASTUtil.getName(method) + "' skipped based on preferences");
            return;
        }

        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        Block body = method.getBody();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Add entry log at the start (if enabled and not skipped)
        if (operation.isStartLoggingEnabled() && !operation.shouldSkipForStart(method)) {
            Statement entryLog = operation.createEntryLogStatement(ast, method);
            listRewrite.insertFirst(entryLog, null);
        }

        // Add exit logs (if enabled and not skipped)
        if (operation.isEndLoggingEnabled() && !operation.shouldSkipForEnd(method)) {
            // Add exit log before each return statement
            addExitLogsBeforeReturns(operation, rewrite, ast, method, body);

            // Add exit log at the end if no return at end
            Statement lastStmt = ASTUtil.getLastStatement(method);
            if (!(lastStmt instanceof ReturnStatement)) {
                Statement exitLog = operation.createExitLogStatement(ast, method);
                listRewrite.insertLast(exitLog, null);
            }
        }

        // Add catch block logging (if enabled)
        if (operation.isCatchLoggingEnabled()) {
            addCatchBlockLogging(operation, rewrite, ast, method);
        }

        applyRewrite(context, rewrite);
        logSuccess("Method '" + ASTUtil.getName(method) + "' logged successfully");
    }

    @SuppressWarnings("unchecked")
    private void addExitLogsBeforeReturns(LoggingOperation operation, ASTRewrite rewrite,
                                           AST ast, MethodDeclaration method, Block body) {
        List<Statement> statements = body.statements();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        for (Statement stmt : statements) {
            if (stmt instanceof ReturnStatement) {
                ReturnStatement returnStmt = (ReturnStatement) stmt;
                Expression returnExpr = returnStmt.getExpression();

                Statement exitLog;
                if (returnExpr != null && operation.includeReturnValue()) {
                    // Try to get return value variable name
                    String returnVarName = extractReturnVariableName(returnExpr);
                    if (returnVarName != null) {
                        exitLog = operation.createExitLogStatementWithReturn(ast, method, returnVarName);
                    } else {
                        exitLog = operation.createExitLogStatement(ast, method);
                    }
                } else {
                    exitLog = operation.createExitLogStatement(ast, method);
                }

                listRewrite.insertBefore(exitLog, stmt, null);
            }
        }
    }

    /**
     * Extracts variable name from return expression if it's a simple name.
     */
    private String extractReturnVariableName(Expression returnExpr) {
        if (returnExpr instanceof org.eclipse.jdt.core.dom.SimpleName) {
            return ((org.eclipse.jdt.core.dom.SimpleName) returnExpr).getIdentifier();
        }
        // For complex expressions, we'd need to create a temp variable
        // For now, just return null to use simple exit log
        return null;
    }

    private void addCatchBlockLogging(LoggingOperation operation, ASTRewrite rewrite,
                                       AST ast, MethodDeclaration method) {
        List<CatchClause> catches = ASTUtil.findCatchClauses(method);
        for (CatchClause catchClause : catches) {
            Block catchBody = catchClause.getBody();
            if (catchBody != null && ASTUtil.isEmptyCatchBlock(catchClause)) {
                // Only add logging to empty catch blocks
                Statement catchLog = operation.createCatchLogStatement(ast, catchClause, method);
                ListRewrite listRewrite = rewrite.getListRewrite(catchBody, Block.STATEMENTS_PROPERTY);
                listRewrite.insertFirst(catchLog, null);
            }
        }
    }
}
