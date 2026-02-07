package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEdit;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for adding entry/exit/catch logging to a method selected in Package Explorer.
 */
public class LogMethodResourceHandler extends BaseMethodResourceHandler {

    @Override
    protected void processMethod(OperationContext context) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();
        LoggingOperation operation = createOperation(context);

        if (operation.shouldSkipForStart(method) && operation.shouldSkipForEnd(method)) {
            Log4e2026Plugin.logWarning("Method '" + ASTUtil.getName(method) + "' skipped based on preferences");
            return;
        }

        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        Block body = method.getBody();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Add entry log
        if (operation.isStartLoggingEnabled() && !operation.shouldSkipForStart(method)) {
            Statement entryLog = operation.createEntryLogStatement(ast, method);
            listRewrite.insertFirst(entryLog, null);
        }

        // Add exit logs before return statements
        if (operation.isEndLoggingEnabled() && !operation.shouldSkipForEnd(method)) {
            addExitLogsBeforeReturns(operation, rewrite, ast, method, body);

            Statement lastStmt = ASTUtil.getLastStatement(method);
            if (!(lastStmt instanceof ReturnStatement)) {
                Statement exitLog = operation.createExitLogStatement(ast, method);
                listRewrite.insertLast(exitLog, null);
            }
        }

        // Add catch block logging
        if (operation.isCatchLoggingEnabled()) {
            List<CatchClause> catches = ASTUtil.findCatchClauses(method);
            for (CatchClause catchClause : catches) {
                Block catchBody = catchClause.getBody();
                if (catchBody != null && ASTUtil.isEmptyCatchBlock(catchClause)) {
                    Statement catchLog = operation.createCatchLogStatement(ast, catchClause, method);
                    ListRewrite catchRewrite = rewrite.getListRewrite(catchBody, Block.STATEMENTS_PROPERTY);
                    catchRewrite.insertFirst(catchLog, null);
                }
            }
        }

        TextEdit edits = rewrite.rewriteAST(context.getDocument(), context.getJavaProject().getOptions(true));
        edits.apply(context.getDocument());
        Log4e2026Plugin.log("Method '" + ASTUtil.getName(method) + "' logged successfully (Package Explorer)");
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
                if (returnExpr != null && operation.includeReturnValue()
                        && returnExpr instanceof SimpleName) {
                    String returnVarName = ((SimpleName) returnExpr).getIdentifier();
                    exitLog = operation.createExitLogStatementWithReturn(ast, method, returnVarName);
                } else {
                    exitLog = operation.createExitLogStatement(ast, method);
                }
                listRewrite.insertBefore(exitLog, stmt, null);
            }
        }
    }
}
