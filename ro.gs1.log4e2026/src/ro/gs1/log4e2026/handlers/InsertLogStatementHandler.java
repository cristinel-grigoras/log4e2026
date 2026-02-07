package ro.gs1.log4e2026.handlers;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.operations.OperationContext;
import ro.gs1.log4e2026.preferences.ProjectPreferences;

/**
 * Handler for inserting a log statement at the current cursor position.
 * Keyboard shortcut: Ctrl+Alt+L
 */
public class InsertLogStatementHandler extends BaseLogHandler {

    @Override
    protected void executeOperation(OperationContext context, String selectedText) throws Exception {
        MethodDeclaration method = context.getSelectedMethod();

        if (method == null || method.getBody() == null) {
            logWarning("No method found at cursor position");
            return;
        }

        // Get project-aware preferences
        ProjectPreferences prefs = Log4e2026Plugin.getProjectPreferences(
                context.getJavaProject().getProject());
        String loggerName = prefs.getLoggerName();
        String delimiter = prefs.getDelimiter();

        // Build the log statement
        String methodName = method.getName().getIdentifier();
        String logStatement = loggerName + ".debug(\"" + methodName + "()" + delimiter + "\");";

        // Create AST rewrite
        AST ast = context.getAstRoot().getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        // Parse the log statement
        ASTParser stmtParser = ASTParser.newParser(AST.getJLSLatest());
        stmtParser.setSource(("class Temp { void m() { " + logStatement + " } }").toCharArray());
        stmtParser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit tempCu = (CompilationUnit) stmtParser.createAST(null);
        org.eclipse.jdt.core.dom.TypeDeclaration tempType =
            (org.eclipse.jdt.core.dom.TypeDeclaration) tempCu.types().get(0);
        MethodDeclaration tempMethod = tempType.getMethods()[0];
        Statement tempStmt = (Statement) tempMethod.getBody().statements().get(0);

        // Copy statement to target AST
        Statement newStmt = (Statement) ASTNode.copySubtree(ast, tempStmt);

        // Find insertion point - insert at cursor position within the method
        Block body = method.getBody();
        ListRewrite listRewrite = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);

        // Find the best insertion point based on cursor offset
        int offset = context.getSelectionOffset();
        int insertIndex = findInsertionIndex(body, offset, context.getAstRoot());

        if (insertIndex >= 0 && insertIndex < body.statements().size()) {
            Statement refStmt = (Statement) body.statements().get(insertIndex);
            listRewrite.insertBefore(newStmt, refStmt, null);
        } else {
            listRewrite.insertLast(newStmt, null);
        }

        // Apply the rewrite
        applyRewrite(context, rewrite);
        logSuccess("Log statement inserted successfully");
    }

    private int findInsertionIndex(Block body, int offset, CompilationUnit astRoot) {
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
