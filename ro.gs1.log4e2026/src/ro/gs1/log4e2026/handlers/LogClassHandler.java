package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;
import ro.gs1.log4e2026.preferences.PreferenceConstants;

/**
 * Handler for adding logging to all methods in the current class.
 * Uses position settings for:
 * - Log levels per position (start/end/catch)
 * - Skip settings (getters/setters/toString/etc.)
 * - Conditional wrapping
 * - Parameter and return value logging
 * Keyboard shortcut: Ctrl+Alt+U
 */
public class LogClassHandler extends BaseLogHandler {

    @Override
    protected String getWizardPreferenceKey() {
        return PreferenceConstants.P_WIZARD_INSERT_CLASS;
    }

    @Override
    protected String getWizardTitle() {
        return "Log This Class";
    }

    @Override
    protected void executeOperation(OperationContext context, String selectedText) throws Exception {
        // Find the type declaration
        ASTNode node = NodeFinder.perform(context.getAstRoot(),
                context.getSelectionOffset(), 0);
        TypeDeclaration typeDecl = findEnclosingType(node);

        if (typeDecl == null) {
            logWarning("No class found at cursor position");
            return;
        }

        LoggingOperation operation = createOperation(context);
        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        int methodCount = 0;
        int skippedCount = 0;

        for (MethodDeclaration method : typeDecl.getMethods()) {
            if (method.getBody() == null) {
                continue;
            }

            // Use preference-based skip checks
            boolean skipStart = operation.shouldSkipForStart(method);
            boolean skipEnd = operation.shouldSkipForEnd(method);

            // If both start and end are skipped, skip the whole method
            if (skipStart && skipEnd) {
                skippedCount++;
                continue;
            }

            logMethod(operation, rewrite, ast, method, skipStart, skipEnd);
            methodCount++;
        }

        if (methodCount > 0) {
            applyRewrite(context, rewrite);
            String msg = "Logged " + methodCount + " methods in class '" + ASTUtil.getName(typeDecl) + "'";
            if (skippedCount > 0) {
                msg += " (" + skippedCount + " skipped based on preferences)";
            }
            logSuccess(msg);
        } else {
            logWarning("No methods to log in class (all skipped based on preferences)");
        }
    }

    @SuppressWarnings("unchecked")
    private void logMethod(LoggingOperation operation, ASTRewrite rewrite,
                           AST ast, MethodDeclaration method,
                           boolean skipStart, boolean skipEnd) {
        Block body = method.getBody();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Add entry log at the start (if not skipped and enabled)
        if (!skipStart && operation.isStartLoggingEnabled()) {
            Statement entryLog = operation.createEntryLogStatement(ast, method);
            listRewrite.insertFirst(entryLog, null);
        }

        // Add exit logs (if not skipped and enabled)
        if (!skipEnd && operation.isEndLoggingEnabled()) {
            // Add exit log before each return statement
            List<Statement> statements = body.statements();
            for (Statement stmt : statements) {
                if (stmt instanceof ReturnStatement) {
                    ReturnStatement returnStmt = (ReturnStatement) stmt;
                    Expression returnExpr = returnStmt.getExpression();

                    Statement exitLog;
                    if (returnExpr != null && operation.includeReturnValue()) {
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

            // Add exit log at the end if no return at end
            Statement lastStmt = ASTUtil.getLastStatement(method);
            if (!(lastStmt instanceof ReturnStatement)) {
                Statement exitLog = operation.createExitLogStatement(ast, method);
                listRewrite.insertLast(exitLog, null);
            }
        }

        // Add catch block logging (if enabled)
        if (operation.isCatchLoggingEnabled()) {
            List<CatchClause> catches = ASTUtil.findCatchClauses(method);
            for (CatchClause catchClause : catches) {
                Block catchBody = catchClause.getBody();
                if (catchBody != null && ASTUtil.isEmptyCatchBlock(catchClause)) {
                    Statement catchLog = operation.createCatchLogStatement(ast, catchClause, method);
                    ListRewrite catchListRewrite = rewrite.getListRewrite(catchBody, Block.STATEMENTS_PROPERTY);
                    catchListRewrite.insertFirst(catchLog, null);
                }
            }
        }
    }

    /**
     * Extracts variable name from return expression if it's a simple name.
     */
    private String extractReturnVariableName(Expression returnExpr) {
        if (returnExpr instanceof SimpleName) {
            return ((SimpleName) returnExpr).getIdentifier();
        }
        return null;
    }
}
