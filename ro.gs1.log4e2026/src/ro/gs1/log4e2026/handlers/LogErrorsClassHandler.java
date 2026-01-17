package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Handler for adding error logging to all catch blocks in the current class.
 * Keyboard shortcut: Ctrl+Alt+E
 */
public class LogErrorsClassHandler extends BaseLogHandler {

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

        int count = 0;

        for (MethodDeclaration method : typeDecl.getMethods()) {
            if (method.getBody() != null) {
                List<CatchClause> catches = ASTUtil.findCatchClauses(method);
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
            }
        }

        if (count > 0) {
            applyRewrite(context, rewrite);
            logSuccess("Added error logging to " + count + " catch blocks in class '"
                    + ASTUtil.getName(typeDecl) + "'");
        } else {
            logWarning("No catch blocks without logging found in class");
        }
    }
}
