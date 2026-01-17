package ro.gs1.log4e2026.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.gs1.log4e2026.jdt.ASTUtil;

/**
 * Property tester for Log4E menu enablement.
 *
 * Tests the following properties:
 * - isCursorInMethod: true if cursor is inside a method body
 * - isCursorOnVariable: true if cursor is on a variable
 * - isValidInsertPosition: true if cursor is at a valid position for insertion
 * - isInJavaEditor: true if active editor is a Java editor
 */
public class Log4ePropertyTester extends PropertyTester {

    private static final String PROP_IN_METHOD = "isCursorInMethod";
    private static final String PROP_ON_VARIABLE = "isCursorOnVariable";
    private static final String PROP_VALID_INSERT = "isValidInsertPosition";
    private static final String PROP_IN_JAVA_EDITOR = "isInJavaEditor";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        try {
            switch (property) {
                case PROP_IN_METHOD:
                    return isCursorInMethod();
                case PROP_ON_VARIABLE:
                    return isCursorOnVariable();
                case PROP_VALID_INSERT:
                    return isValidInsertPosition();
                case PROP_IN_JAVA_EDITOR:
                    return isInJavaEditor();
                default:
                    return false;
            }
        } catch (Exception e) {
            // If any error occurs during testing, return false (disable menu item)
            return false;
        }
    }

    private boolean isInJavaEditor() {
        IEditorPart editor = getActiveEditor();
        if (editor == null) {
            return false;
        }
        IEditorInput input = editor.getEditorInput();
        return input != null && JavaUI.getEditorInputJavaElement(input) instanceof ICompilationUnit;
    }

    private boolean isCursorInMethod() {
        ICompilationUnit cu = getActiveCompilationUnit();
        if (cu == null) {
            return false;
        }
        int offset = getSelectionOffset();
        if (offset < 0) {
            return false;
        }
        CompilationUnit astRoot = ASTUtil.parseCompilationUnit(cu);
        return ASTUtil.isCursorInMethod(astRoot, offset);
    }

    private boolean isCursorOnVariable() {
        ICompilationUnit cu = getActiveCompilationUnit();
        if (cu == null) {
            return false;
        }
        int offset = getSelectionOffset();
        if (offset < 0) {
            return false;
        }
        CompilationUnit astRoot = ASTUtil.parseCompilationUnit(cu);
        // First check if we're in a method
        if (!ASTUtil.isCursorInMethod(astRoot, offset)) {
            return false;
        }
        return ASTUtil.isCursorOnVariable(astRoot, offset);
    }

    private boolean isValidInsertPosition() {
        ICompilationUnit cu = getActiveCompilationUnit();
        if (cu == null) {
            return false;
        }
        int[] selection = getSelection();
        if (selection == null) {
            return false;
        }
        CompilationUnit astRoot = ASTUtil.parseCompilationUnit(cu);
        // First check if we're in a method
        if (!ASTUtil.isCursorInMethod(astRoot, selection[0])) {
            return false;
        }
        return ASTUtil.isValidInsertPosition(astRoot, selection[0], selection[1]);
    }

    private IEditorPart getActiveEditor() {
        try {
            return PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .getActiveEditor();
        } catch (Exception e) {
            return null;
        }
    }

    private ICompilationUnit getActiveCompilationUnit() {
        IEditorPart editor = getActiveEditor();
        if (editor == null) {
            return null;
        }
        IEditorInput input = editor.getEditorInput();
        if (input == null) {
            return null;
        }
        return (ICompilationUnit) JavaUI.getEditorInputJavaElement(input);
    }

    private int getSelectionOffset() {
        int[] selection = getSelection();
        return selection != null ? selection[0] : -1;
    }

    private int[] getSelection() {
        IEditorPart editor = getActiveEditor();
        if (!(editor instanceof ITextEditor)) {
            return null;
        }
        ITextEditor textEditor = (ITextEditor) editor;
        ISelection selection = textEditor.getSelectionProvider().getSelection();
        if (!(selection instanceof ITextSelection)) {
            return null;
        }
        ITextSelection textSelection = (ITextSelection) selection;
        return new int[] { textSelection.getOffset(), textSelection.getLength() };
    }
}
