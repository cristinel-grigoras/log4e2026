package ro.gs1.log4e2026.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;
import ro.gs1.log4e2026.wizard.ChangeElement;
import ro.gs1.log4e2026.wizard.LoggerWizardDialog;

/**
 * Base class for logging operation handlers.
 */
public abstract class BaseLogHandler extends AbstractHandler {

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
        int offset = 0;
        int length = 0;
        String selectedText = null;

        if (selection instanceof ITextSelection) {
            ITextSelection textSelection = (ITextSelection) selection;
            offset = textSelection.getOffset();
            length = textSelection.getLength();
            selectedText = textSelection.getText();
        }

        try {
            // Get the document
            IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

            // Check if wizard preview is enabled
            String wizardPref = getWizardPreferenceKey();
            boolean showWizard = wizardPref != null && Log4e2026Plugin.getPreferences().getBoolean(wizardPref);

            if (showWizard) {
                // Execute with wizard preview
                executeWithWizardPreview(compilationUnit, document, offset, length, selectedText);
            } else {
                // Execute directly
                OperationContext context = createContext(compilationUnit, document, offset, length);
                executeOperation(context, selectedText);
            }

        } catch (Exception e) {
            Log4e2026Plugin.logError("Failed to execute operation", e);
            throw new ExecutionException("Failed to execute operation", e);
        }

        return null;
    }

    /**
     * Execute the operation with wizard preview.
     */
    private void executeWithWizardPreview(ICompilationUnit cu, IDocument document,
            int offset, int length, String selectedText) throws Exception {

        // Save old content
        String oldContent = document.get();

        // Create context and execute to get preview
        OperationContext context = createContext(cu, document, offset, length);
        executeOperation(context, selectedText);

        // Get preview content
        String previewContent = document.get();

        // Revert to old content
        document.set(oldContent);

        // Check if there are changes
        if (oldContent.equals(previewContent)) {
            logWarning("No changes to apply");
            return;
        }

        // Show wizard
        ChangeElement change = new ChangeElement(cu, oldContent, previewContent);
        Shell shell = Display.getCurrent().getActiveShell();
        if (!LoggerWizardDialog.openPreview(shell, change, getWizardTitle())) {
            return; // User cancelled
        }

        // User approved - re-execute the operation
        // Need to re-parse since document was reverted
        context = createContext(cu, document, offset, length);
        executeOperation(context, selectedText);
    }

    /**
     * Create an operation context.
     */
    private OperationContext createContext(ICompilationUnit cu, IDocument document,
            int offset, int length) throws Exception {
        OperationContext context = new OperationContext();
        context.setCompilationUnit(cu);
        context.setSelectionOffset(offset);
        context.setSelectionLength(length);

        // Parse the compilation unit
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
        context.setAstRoot(astRoot);

        context.setDocument(document);

        // Find the enclosing method
        ASTNode node = NodeFinder.perform(astRoot, offset, 0);
        MethodDeclaration method = findEnclosingMethod(node);
        context.setSelectedMethod(method);

        return context;
    }

    /**
     * Returns the preference key for the wizard, or null if no wizard.
     * Subclasses should override this to enable wizard support.
     */
    protected String getWizardPreferenceKey() {
        return null;
    }

    /**
     * Returns the title for the wizard dialog.
     * Subclasses should override this to provide a meaningful title.
     */
    protected String getWizardTitle() {
        return "Log4E Operation";
    }

    /**
     * Execute the specific logging operation.
     */
    protected abstract void executeOperation(OperationContext context, String selectedText) throws Exception;

    /**
     * Finds the enclosing method for a node.
     */
    protected MethodDeclaration findEnclosingMethod(ASTNode node) {
        while (node != null) {
            if (node instanceof MethodDeclaration) {
                return (MethodDeclaration) node;
            }
            node = node.getParent();
        }
        return null;
    }

    /**
     * Finds the enclosing type for a node.
     */
    protected TypeDeclaration findEnclosingType(ASTNode node) {
        while (node != null) {
            if (node instanceof TypeDeclaration) {
                return (TypeDeclaration) node;
            }
            node = node.getParent();
        }
        return null;
    }

    /**
     * Applies the AST rewrite to the document.
     */
    protected void applyRewrite(OperationContext context, ASTRewrite rewrite) throws Exception {
        TextEdit edits = rewrite.rewriteAST(context.getDocument(),
                context.getJavaProject().getOptions(true));
        edits.apply(context.getDocument());
    }

    /**
     * Creates a new logging operation for the context.
     */
    protected LoggingOperation createOperation(OperationContext context) {
        return new LoggingOperation(context);
    }

    /**
     * Logs a success message.
     */
    protected void logSuccess(String message) {
        Log4e2026Plugin.log(message);
    }

    /**
     * Logs a warning message.
     */
    protected void logWarning(String message) {
        Log4e2026Plugin.logWarning(message);
    }
}
