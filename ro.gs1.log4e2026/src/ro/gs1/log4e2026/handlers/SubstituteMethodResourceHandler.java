package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for replacing System.out/err with logger calls in a method selected in Package Explorer.
 */
public class SubstituteMethodResourceHandler extends BaseMethodResourceHandler {

    @Override
    protected void processMethod(OperationContext context) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();
        LoggingOperation operation = createOperation(context);

        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        final int[] count = {0};
        Block body = method.getBody();

        body.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (operation.isSystemPrintln(node)) {
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
            TextEdit edits = rewrite.rewriteAST(context.getDocument(), context.getJavaProject().getOptions(true));
            edits.apply(context.getDocument());
            Log4e2026Plugin.log("Replaced " + count[0] + " System.out/err calls in method '"
                    + ASTUtil.getName(method) + "' (Package Explorer)");
        } else {
            Log4e2026Plugin.logWarning("No System.out/err calls found in method");
        }
    }
}
