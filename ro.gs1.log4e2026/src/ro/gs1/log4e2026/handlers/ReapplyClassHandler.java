package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for reapplying logging to all methods in the current class.
 * Keyboard shortcut: Ctrl+Alt+N
 */
public class ReapplyClassHandler extends BaseLogHandler {

    @Override
    protected void executeOperation(OperationContext context, String selectedText) throws Exception {
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
        for (MethodDeclaration method : typeDecl.getMethods()) {
            if (method.getBody() != null && !method.isConstructor()) {
                if (ASTUtil.isGetter(method) || ASTUtil.isSetter(method)) {
                    continue;
                }
                if (ASTUtil.isToStringMethod(method) || ASTUtil.isHashCodeMethod(method)
                        || ASTUtil.isEqualsMethod(method)) {
                    continue;
                }

                reapplyMethod(operation, rewrite, ast, method);
                methodCount++;
            }
        }

        if (methodCount > 0) {
            applyRewrite(context, rewrite);
            logSuccess("Logging reapplied to " + methodCount + " methods in class '"
                    + ASTUtil.getName(typeDecl) + "'");
        } else {
            logWarning("No methods to reapply logging in class");
        }
    }

    @SuppressWarnings("unchecked")
    private void reapplyMethod(LoggingOperation operation, ASTRewrite rewrite,
                               AST ast, MethodDeclaration method) {
        Block body = method.getBody();

        // First remove existing log statements
        operation.removeLogStatements(rewrite, body);

        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Add entry log at the start
        Statement entryLog = operation.createEntryLogStatement(ast, method);
        listRewrite.insertFirst(entryLog, null);

        // Add exit log before each return statement
        List<Statement> statements = body.statements();
        for (Statement stmt : statements) {
            if (stmt instanceof ReturnStatement) {
                Statement exitLog = operation.createExitLogStatement(ast, method);
                listRewrite.insertBefore(exitLog, stmt, null);
            }
        }

        // Add exit log at the end if no return at end
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
    }
}
