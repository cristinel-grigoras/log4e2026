package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for logging a selected variable.
 * Keyboard shortcut: Ctrl+Alt+P
 */
public class LogVariableHandler extends BaseLogHandler {

    @Override
    protected void executeOperation(OperationContext context, String selectedText) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();

        if (method == null || method.getBody() == null) {
            logWarning("No method found at cursor position");
            return;
        }

        String variableName = selectedText;
        if (variableName == null || variableName.trim().isEmpty()) {
            // Try to find variable at cursor position
            ASTNode node = NodeFinder.perform(context.getAstRoot(),
                    context.getSelectionOffset(), context.getSelectionLength());
            variableName = findVariableAtNode(node);
        }

        if (variableName == null || variableName.trim().isEmpty()) {
            logWarning("No variable selected");
            return;
        }

        variableName = variableName.trim();

        LoggingOperation operation = createOperation(context);
        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        String methodName = ASTUtil.getName(method);
        Statement logStmt = operation.createVariableLogStatement(ast, variableName, methodName);

        // Insert after the variable declaration or at cursor position
        Block body = method.getBody();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Find the best insertion point
        int insertIdx = findInsertionPointForVariable(body, context.getSelectionOffset(), variableName);
        if (insertIdx >= 0 && insertIdx < body.statements().size()) {
            Statement refStmt = (Statement) body.statements().get(insertIdx);
            listRewrite.insertBefore(logStmt, refStmt, null);
        } else {
            listRewrite.insertLast(logStmt, null);
        }

        applyRewrite(context, rewrite);
        logSuccess("Variable '" + variableName + "' logged successfully");
    }

    private String findVariableAtNode(ASTNode node) {
        if (node == null) {
            return null;
        }
        if (node instanceof org.eclipse.jdt.core.dom.SimpleName) {
            return ((org.eclipse.jdt.core.dom.SimpleName) node).getIdentifier();
        }
        if (node instanceof VariableDeclarationFragment) {
            return ASTUtil.getName((VariableDeclarationFragment) node);
        }
        // Check parent
        ASTNode parent = node.getParent();
        if (parent instanceof VariableDeclarationFragment) {
            return ASTUtil.getName((VariableDeclarationFragment) parent);
        }
        return null;
    }

    private int findInsertionPointForVariable(Block body, int offset, String variableName) {
        @SuppressWarnings("unchecked")
        java.util.List<Statement> statements = body.statements();
        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements.get(i);
            if (stmt instanceof VariableDeclarationStatement) {
                VariableDeclarationStatement vds = (VariableDeclarationStatement) stmt;
                for (Object fragment : vds.fragments()) {
                    VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                    if (variableName.equals(ASTUtil.getName(vdf))) {
                        // Insert after this statement
                        return i + 1;
                    }
                }
            }
            if (stmt.getStartPosition() >= offset) {
                return i;
            }
        }
        return statements.size();
    }
}
