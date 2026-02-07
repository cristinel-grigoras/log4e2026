package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for removing log statements from a method selected in Package Explorer.
 */
public class RemoveLoggerMethodResourceHandler extends BaseMethodResourceHandler {

    @Override
    protected void processMethod(OperationContext context) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();
        LoggingOperation operation = createOperation(context);

        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        Block body = method.getBody();
        operation.removeLogStatementsAdvanced(rewrite, body);

        TextEdit edits = rewrite.rewriteAST(context.getDocument(), context.getJavaProject().getOptions(true));
        edits.apply(context.getDocument());
        Log4e2026Plugin.log("Log statements removed from method '" + ASTUtil.getName(method) + "' (Package Explorer)");
    }
}
