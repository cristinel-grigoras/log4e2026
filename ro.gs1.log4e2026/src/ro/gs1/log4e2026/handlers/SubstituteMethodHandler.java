package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for substituting System.out/err.println with logging in the current method.
 * Keyboard shortcut: Ctrl+Alt+S
 */
public class SubstituteMethodHandler extends BaseLogHandler {

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

        final int[] count = {0};
        Block body = method.getBody();

        body.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (operation.isSystemPrintln(node)) {
                    // Determine log level based on System.out vs System.err
                    String expr = node.getExpression().toString();
                    LogLevel level = "System.err".equals(expr) ? LogLevel.ERROR : LogLevel.INFO;

                    Statement parent = (Statement) node.getParent();
                    if (parent instanceof ExpressionStatement) {
                        Statement replacement = operation.replaceSystemPrintln(ast, node, level);
                        rewrite.replace(parent, replacement, null);
                        count[0]++;
                    }
                }
                return true;
            }
        });

        if (count[0] > 0) {
            applyRewrite(context, rewrite);
            logSuccess("Replaced " + count[0] + " System.out/err calls in method '"
                    + ASTUtil.getName(method) + "'");
        } else {
            logWarning("No System.out/err calls found in method");
        }
    }
}
