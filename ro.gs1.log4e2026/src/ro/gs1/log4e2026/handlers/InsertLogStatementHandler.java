package ro.gs1.log4e2026.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.preferences.PreferenceConstants;

/**
 * Handler for inserting a log statement at the current cursor position.
 * Keyboard shortcut: Ctrl+Alt+L
 */
public class InsertLogStatementHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);

        if (!(editor instanceof ITextEditor)) {
            return null;
        }

        ITextEditor textEditor = (ITextEditor) editor;
        ICompilationUnit compilationUnit = JavaUI.getWorkingCopyManager()
                .getWorkingCopy(textEditor.getEditorInput());

        if (compilationUnit == null) {
            return null;
        }

        ISelection selection = textEditor.getSelectionProvider().getSelection();
        if (!(selection instanceof ITextSelection)) {
            return null;
        }

        ITextSelection textSelection = (ITextSelection) selection;

        try {
            insertLogStatement(compilationUnit, textEditor, textSelection.getOffset());
        } catch (Exception e) {
            Log4e2026Plugin.logError("Failed to insert log statement", e);
            throw new ExecutionException("Failed to insert log statement", e);
        }

        return null;
    }

    private void insertLogStatement(ICompilationUnit cu, ITextEditor editor, int offset) throws Exception {
        // Parse the compilation unit
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        // Find the enclosing method
        ASTNode node = NodeFinder.perform(astRoot, offset, 0);
        MethodDeclaration method = findEnclosingMethod(node);

        if (method == null || method.getBody() == null) {
            Log4e2026Plugin.logWarning("No method found at cursor position");
            return;
        }

        // Get preferences
        String loggerName = Log4e2026Plugin.getPreferences()
                .getString(PreferenceConstants.P_LOGGER_NAME);
        String delimiter = Log4e2026Plugin.getPreferences()
                .getString(PreferenceConstants.P_DELIMITER);

        // Build the log statement
        String methodName = method.getName().getIdentifier();
        String logStatement = loggerName + ".debug(\"" + methodName + "()" + delimiter + "\");";

        // Create AST rewrite
        AST ast = astRoot.getAST();
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
        int insertIndex = findInsertionIndex(body, offset, astRoot);

        if (insertIndex >= 0 && insertIndex < body.statements().size()) {
            Statement refStmt = (Statement) body.statements().get(insertIndex);
            listRewrite.insertBefore(newStmt, refStmt, null);
        } else {
            listRewrite.insertLast(newStmt, null);
        }

        // Apply the rewrite
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        TextEdit edits = rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
        edits.apply(document);

        Log4e2026Plugin.log("Log statement inserted successfully");
    }

    private MethodDeclaration findEnclosingMethod(ASTNode node) {
        while (node != null) {
            if (node instanceof MethodDeclaration) {
                return (MethodDeclaration) node;
            }
            node = node.getParent();
        }
        return null;
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
