package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
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
import ro.gs1.log4e2026.preferences.ProjectPreferences;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;
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

            // Auto-declare logger if needed (before the operation)
            if (shouldAutoDeclare()) {
                int lengthBefore = document.getLength();
                boolean declared = ensureLoggerDeclared(compilationUnit, document);
                if (declared) {
                    // Adjust offset to account for added imports and field declaration
                    int delta = document.getLength() - lengthBefore;
                    offset += delta;
                    // Reconcile the compilation unit with document changes
                    compilationUnit.reconcile(ICompilationUnit.NO_AST, false, null, null);
                }
            }

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
     * Check if automatic logger declaration should be performed.
     * Subclasses can override to disable auto-declaration.
     */
    protected boolean shouldAutoDeclare() {
        return true;
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

        // Sync working copy buffer with document to ensure AST reflects latest changes
        cu.getBuffer().setContents(document.get());

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

    /**
     * Ensures a logger is declared in the current class if automatic declaration is enabled.
     * @return true if the logger was declared, false otherwise
     */
    protected boolean ensureLoggerDeclared(ICompilationUnit cu, IDocument document) throws Exception {
        // Get project preferences
        ProjectPreferences prefs = Log4e2026Plugin.getProjectPreferences(
                cu.getJavaProject().getProject());

        // Check if automatic declaration is enabled
        if (!prefs.isAutomaticDeclareEnabled()) {
            return false;
        }

        IType primaryType = cu.findPrimaryType();
        if (primaryType == null) {
            return false;
        }

        // Parse the compilation unit
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        // Get preferences
        String framework = prefs.getLoggingFramework();
        String loggerName = prefs.getLoggerName();

        // Get the template
        LoggerTemplate template = LoggerTemplates.getTemplate(framework);
        if (template == null) {
            template = LoggerTemplates.getSLF4J();
        }

        // Find the type declaration
        if (astRoot.types().isEmpty()) {
            return false;
        }
        TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);

        // Check if logger already exists
        if (isLoggerDeclared(typeDecl, loggerName)) {
            return false;
        }

        Log4e2026Plugin.log("Auto-declaring logger '" + loggerName + "'");

        // Create AST rewrite
        AST ast = astRoot.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        // Add imports if enabled
        if (prefs.isAutomaticImportsEnabled()) {
            addImportsIfNeeded(astRoot, ast, rewrite, template);
        }

        // Create the logger field declaration
        String className = primaryType.getElementName();
        FieldDeclaration loggerField = createLoggerField(ast, template, loggerName, className);

        // Add the field at the appropriate position
        ListRewrite bodyRewrite = rewrite.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        int insertIndex = findInsertPosition(typeDecl);
        if (insertIndex == 0) {
            bodyRewrite.insertFirst(loggerField, null);
        } else {
            bodyRewrite.insertAt(loggerField, insertIndex, null);
        }

        // Apply the changes
        TextEdit edits = rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
        edits.apply(document);
        Log4e2026Plugin.log("Logger auto-declared successfully");
        return true;
    }

    /**
     * Check if logger is already declared in the type.
     */
    private boolean isLoggerDeclared(TypeDeclaration typeDecl, String loggerName) {
        for (FieldDeclaration field : typeDecl.getFields()) {
            for (Object fragment : field.fragments()) {
                VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                if (vdf.getName().getIdentifier().equals(loggerName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add import declarations if they don't already exist.
     */
    @SuppressWarnings("unchecked")
    private void addImportsIfNeeded(CompilationUnit astRoot, AST ast, ASTRewrite rewrite,
            LoggerTemplate template) {

        String[] imports = template.getImports();
        if (imports == null || imports.length == 0) {
            return;
        }

        ListRewrite importRewrite = rewrite.getListRewrite(astRoot, CompilationUnit.IMPORTS_PROPERTY);

        for (String importName : imports) {
            if (!hasImport(astRoot, importName)) {
                ImportDeclaration importDecl = ast.newImportDeclaration();
                importDecl.setName(ast.newName(importName.split("\\.")));
                importRewrite.insertLast(importDecl, null);
            }
        }
    }

    /**
     * Check if an import already exists in the compilation unit.
     */
    private boolean hasImport(CompilationUnit astRoot, String importName) {
        @SuppressWarnings("unchecked")
        List<ImportDeclaration> imports = astRoot.imports();
        for (ImportDeclaration imp : imports) {
            String existingImport = imp.getName().getFullyQualifiedName();
            if (existingImport.equals(importName)) {
                return true;
            }
            // Check for wildcard imports
            if (imp.isOnDemand()) {
                String packageName = importName.substring(0, importName.lastIndexOf('.'));
                if (existingImport.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a logger field declaration.
     */
    private FieldDeclaration createLoggerField(AST ast, LoggerTemplate template,
            String loggerName, String className) {

        // Get declaration template and substitute variables
        String declaration = template.getDeclaration()
                .replace("${enclosing_type}", className)
                .replace("${logger}", loggerName);

        // Parse the field declaration using a temporary class
        ASTParser fieldParser = ASTParser.newParser(AST.getJLSLatest());
        fieldParser.setSource(("class Temp { " + declaration + " }").toCharArray());
        fieldParser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit tempCu = (CompilationUnit) fieldParser.createAST(null);
        TypeDeclaration tempType = (TypeDeclaration) tempCu.types().get(0);
        FieldDeclaration tempField = tempType.getFields()[0];

        // Copy the field to the target AST
        return (FieldDeclaration) ASTNode.copySubtree(ast, tempField);
    }

    /**
     * Find the best position to insert the logger field.
     */
    private int findInsertPosition(TypeDeclaration typeDecl) {
        FieldDeclaration[] fields = typeDecl.getFields();

        if (fields.length == 0) {
            return 0;
        }

        // Find the last static field and insert after it
        int lastStaticFieldIndex = -1;
        @SuppressWarnings("rawtypes")
        List bodyDeclarations = typeDecl.bodyDeclarations();

        for (int i = 0; i < bodyDeclarations.size(); i++) {
            Object decl = bodyDeclarations.get(i);
            if (decl instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) decl;
                if ((field.getModifiers() & Modifier.STATIC) != 0) {
                    lastStaticFieldIndex = i;
                }
            }
        }

        return lastStaticFieldIndex + 1;
    }
}
