package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEdit;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for adding error logging to catch blocks of a method selected in Package Explorer.
 */
public class LogErrorsMethodResourceHandler extends BaseMethodResourceHandler {

    @Override
    protected void processMethod(OperationContext context) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();
        LoggingOperation operation = createOperation(context);

        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        List<CatchClause> catches = ASTUtil.findCatchClauses(method);
        int count = 0;

        for (CatchClause catchClause : catches) {
            Block catchBody = catchClause.getBody();
            if (catchBody != null) {
                boolean hasLogStatement = false;
                for (Object obj : catchBody.statements()) {
                    if (operation.isLogStatement((Statement) obj)) {
                        hasLogStatement = true;
                        break;
                    }
                }

                if (!hasLogStatement) {
                    Statement catchLog = operation.createCatchLogStatement(ast, catchClause, method);
                    ListRewrite listRewrite = rewrite.getListRewrite(catchBody, Block.STATEMENTS_PROPERTY);
                    listRewrite.insertFirst(catchLog, null);
                    count++;
                }
            }
        }

        if (count > 0) {
            TextEdit edits = rewrite.rewriteAST(context.getDocument(), context.getJavaProject().getOptions(true));
            edits.apply(context.getDocument());
            Log4e2026Plugin.log("Added error logging to " + count + " catch blocks in method '"
                    + ASTUtil.getName(method) + "' (Package Explorer)");
        } else {
            Log4e2026Plugin.logWarning("No catch blocks without logging found in method");
        }
    }
}
