package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for removing logging from all methods in the current class.
 * Performs complete removal including:
 * - All log statements (including conditionally wrapped)
 * - Logger field declaration
 * - Logger-related imports (if logger is no longer used)
 */
public class RemoveLoggerClassHandler extends BaseLogHandler {

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
        CompilationUnit cu = context.getAstRoot();

        // Use complete removal: log statements + declaration + imports
        operation.removeLoggerComplete(rewrite, typeDecl, cu);

        applyRewrite(context, rewrite);
        logSuccess("Logger completely removed from class '" + ASTUtil.getName(typeDecl) +
                "' (statements, declaration, and imports)");
    }
}
