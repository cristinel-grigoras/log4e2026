package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for inserting a log statement at a specific position.
 * Opens a dialog to configure the log level and message.
 * Keyboard shortcut: Ctrl+Alt+O
 */
public class LogAtPositionHandler extends BaseLogHandler {

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

        String methodName = ASTUtil.getName(method);

        // Create a debug-level log statement at the cursor position
        // In a full implementation, this would open a dialog to configure the message
        String message = methodName + "() - position";
        Statement logStmt = operation.createLogStatement(ast, LogLevel.DEBUG, message);

        Block body = method.getBody();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Find insertion point based on cursor offset
        int insertIdx = findInsertionIndex(body, context.getSelectionOffset());
        if (insertIdx >= 0 && insertIdx < body.statements().size()) {
            Statement refStmt = (Statement) body.statements().get(insertIdx);
            listRewrite.insertBefore(logStmt, refStmt, null);
        } else {
            listRewrite.insertLast(logStmt, null);
        }

        applyRewrite(context, rewrite);
        logSuccess("Log statement inserted at position in method '" + methodName + "'");
    }

    private int findInsertionIndex(Block body, int offset) {
        @SuppressWarnings("unchecked")
        java.util.List<Statement> statements = body.statements();
        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements.get(i);
            int stmtStart = stmt.getStartPosition();
            if (stmtStart >= offset) {
                return i;
            }
        }
        return statements.size();
    }
}
