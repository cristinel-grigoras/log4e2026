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
import org.eclipse.text.edits.TextEdit;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for reapplying logging (remove + re-add) to a method selected in Package Explorer.
 */
public class ReapplyMethodResourceHandler extends BaseMethodResourceHandler {

    @Override
    protected void processMethod(OperationContext context) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();
        LoggingOperation operation = createOperation(context);

        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        Block body = method.getBody();

        // Remove existing log statements
        operation.removeLogStatements(rewrite, body);

        // Re-add log statements
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Add entry log
        Statement entryLog = operation.createEntryLogStatement(ast, method);
        listRewrite.insertFirst(entryLog, null);

        // Add exit logs before returns
        addExitLogsBeforeReturns(operation, rewrite, ast, method, body);

        // Add exit log at end if no return at end
        Statement lastStmt = ASTUtil.getLastStatement(method);
        if (!(lastStmt instanceof ReturnStatement)) {
            Statement exitLog = operation.createExitLogStatement(ast, method);
            listRewrite.insertLast(exitLog, null);
        }

        // Handle catch blocks
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

        TextEdit edits = rewrite.rewriteAST(context.getDocument(), context.getJavaProject().getOptions(true));
        edits.apply(context.getDocument());
        Log4e2026Plugin.log("Logging reapplied to method '" + ASTUtil.getName(method) + "' (Package Explorer)");
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
