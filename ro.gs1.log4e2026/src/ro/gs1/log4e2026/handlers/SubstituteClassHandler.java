package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for substituting System.out/err.println with logging in the entire class.
 * Keyboard shortcut: Ctrl+Alt+A
 */
public class SubstituteClassHandler extends BaseLogHandler {

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

        final int[] count = {0};

        typeDecl.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation invocation) {
                if (operation.isSystemPrintln(invocation)) {
                    String expr = invocation.getExpression().toString();
                    LogLevel level = "System.err".equals(expr) ? LogLevel.ERROR : LogLevel.INFO;

                    Statement parent = (Statement) invocation.getParent();
                    if (parent instanceof ExpressionStatement) {
                        Statement replacement = operation.replaceSystemPrintln(ast, invocation, level);
                        rewrite.replace(parent, replacement, null);
                        count[0]++;
                    }
                }
                return true;
            }
        });

        if (count[0] > 0) {
            applyRewrite(context, rewrite);
            logSuccess("Replaced " + count[0] + " System.out/err calls in class '"
                    + ASTUtil.getName(typeDecl) + "'");
        } else {
            logWarning("No System.out/err calls found in class");
        }
    }
}
