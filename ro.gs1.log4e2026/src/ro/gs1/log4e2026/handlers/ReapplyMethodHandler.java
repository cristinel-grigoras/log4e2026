package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for reapplying logging to the current method.
 * First removes existing log statements, then adds new ones.
 * Keyboard shortcut: Ctrl+Alt+M
 */
public class ReapplyMethodHandler extends BaseLogHandler {

    @Override
    protected void executeOperation(OperationContext context, String selectedText) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();

        if (method == null || method.getBody() == null) {
            logWarning("No method found at cursor position");
            return;
        }

        LoggingOperation operation = createOperation(context);
        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        Block body = method.getBody();

        // First, remove existing log statements
        operation.removeLogStatements(rewrite, body);

        // Then add new log statements
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Add entry log at the start
        Statement entryLog = operation.createEntryLogStatement(ast, method);
        listRewrite.insertFirst(entryLog, null);

        // Add exit log before each return statement
        addExitLogsBeforeReturns(operation, rewrite, ast, method, body);

        // Add exit log at the end if no return at end
        Statement lastStmt = ASTUtil.getLastStatement(method);
        if (!(lastStmt instanceof ReturnStatement)) {
            Statement exitLog = operation.createExitLogStatement(ast, method);
            listRewrite.insertLast(exitLog, null);
        }

        // Add catch block logging
        List<CatchClause> catches = ASTUtil.findCatchClauses(method);
        for (CatchClause catchClause : catches) {
            Block catchBody = catchClause.getBody();
            if (catchBody != null) {
                operation.removeLogStatements(rewrite, catchBody);
                if (ASTUtil.isEmptyCatchBlock(catchClause)) {
                    Statement catchLog = operation.createCatchLogStatement(ast, catchClause, method);
                    ListRewrite catchListRewrite = rewrite.getListRewrite(catchBody, Block.STATEMENTS_PROPERTY);
                    catchListRewrite.insertFirst(catchLog, null);
                }
            }
        }

        applyRewrite(context, rewrite);
        logSuccess("Logging reapplied to method '" + ASTUtil.getName(method) + "'");
    }

    @SuppressWarnings("unchecked")
    private void addExitLogsBeforeReturns(LoggingOperation operation, ASTRewrite rewrite,
                                           AST ast, MethodDeclaration method, Block body) {
        List<Statement> statements = body.statements();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        for (Statement stmt : statements) {
            if (stmt instanceof ReturnStatement) {
                Statement exitLog = operation.createExitLogStatement(ast, method);
                listRewrite.insertBefore(exitLog, stmt, null);
            }
        }
    }
}
