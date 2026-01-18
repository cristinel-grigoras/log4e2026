package ro.gs1.log4e2026.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.preferences.PreferenceConstants;
import ro.gs1.log4e2026.preferences.ProjectPreferences;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;
import ro.gs1.log4e2026.wizard.ChangeElement;
import ro.gs1.log4e2026.wizard.LoggerWizardDialog;

/**
 * Handler for declaring a logger field in the current class.
 * Keyboard shortcut: Ctrl+Alt+D
 */
public class DeclareLoggerHandler extends AbstractHandler {

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

        try {
            declareLogger(compilationUnit, textEditor);
        } catch (Exception e) {
            Log4e2026Plugin.logError("Failed to declare logger", e);
            throw new ExecutionException("Failed to declare logger", e);
        }

        return null;
    }

    private void declareLogger(ICompilationUnit cu, ITextEditor editor) throws Exception {
        IType primaryType = cu.findPrimaryType();
        if (primaryType == null) {
            return;
        }

        // Parse the compilation unit
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        // Get project-aware preferences
        ProjectPreferences prefs = Log4e2026Plugin.getProjectPreferences(
                cu.getJavaProject().getProject());
        String framework = prefs.getLoggingFramework();
        String loggerName = prefs.getLoggerName();

        // Get the template
        LoggerTemplate template = LoggerTemplates.getTemplate(framework);
        if (template == null) {
            template = LoggerTemplates.getSLF4J();
        }

        // Find the type declaration
        if (astRoot.types().isEmpty()) {
            return;
        }
        TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);

        // Check if logger already exists
        for (FieldDeclaration field : typeDecl.getFields()) {
            for (Object fragment : field.fragments()) {
                VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                if (vdf.getName().getIdentifier().equals(loggerName)) {
                    Log4e2026Plugin.log("Logger field '" + loggerName + "' already exists");
                    return;
                }
            }
        }

        // Get document and old content for preview
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        String oldContent = document.get();

        // Create AST rewrite
        AST ast = astRoot.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        // Add imports
        addImportsIfNeeded(astRoot, ast, rewrite, template, framework);

        // Create the logger field declaration
        String className = primaryType.getElementName();
        FieldDeclaration loggerField = createLoggerField(ast, template, loggerName, className);

        // Add the field at the beginning of the type body (after any existing fields of same type)
        ListRewrite bodyRewrite = rewrite.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

        // Find position: insert after existing static fields, before other members
        int insertIndex = findInsertPosition(typeDecl);
        if (insertIndex == 0) {
            bodyRewrite.insertFirst(loggerField, null);
        } else {
            bodyRewrite.insertAt(loggerField, insertIndex, null);
        }

        // Check if wizard preview is enabled
        boolean showWizard = Log4e2026Plugin.getPreferences()
                .getBoolean(PreferenceConstants.P_WIZARD_DECLARE_CLASS);

        if (showWizard) {
            // Apply edits to get preview content
            TextEdit edits = rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
            edits.apply(document);
            String previewContent = document.get();

            // Revert to old content
            document.set(oldContent);

            // Show wizard
            ChangeElement change = new ChangeElement(cu, oldContent, previewContent);
            Shell shell = Display.getCurrent().getActiveShell();
            if (!LoggerWizardDialog.openPreview(shell, change, "Declare Logger")) {
                return; // User cancelled
            }

            // User approved - need to re-parse and re-apply since document was reverted
            parser = ASTParser.newParser(AST.getJLSLatest());
            parser.setSource(cu);
            parser.setResolveBindings(true);
            astRoot = (CompilationUnit) parser.createAST(null);
            typeDecl = (TypeDeclaration) astRoot.types().get(0);
            ast = astRoot.getAST();
            rewrite = ASTRewrite.create(ast);

            addImportsIfNeeded(astRoot, ast, rewrite, template, framework);
            loggerField = createLoggerField(ast, template, loggerName, className);
            bodyRewrite = rewrite.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            insertIndex = findInsertPosition(typeDecl);
            if (insertIndex == 0) {
                bodyRewrite.insertFirst(loggerField, null);
            } else {
                bodyRewrite.insertAt(loggerField, insertIndex, null);
            }
        }

        // Apply the changes
        TextEdit edits = rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
        edits.apply(document);
        Log4e2026Plugin.log("Logger declared successfully");
    }

    /**
     * Add import declarations if they don't already exist.
     */
    private void addImportsIfNeeded(CompilationUnit astRoot, AST ast, ASTRewrite rewrite,
            LoggerTemplate template, String framework) {

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
    @SuppressWarnings("unchecked")
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
        return (FieldDeclaration) org.eclipse.jdt.core.dom.ASTNode.copySubtree(ast, tempField);
    }

    /**
     * Find the best position to insert the logger field.
     * Returns 0 to insert at the beginning, or an index after existing static fields.
     */
    private int findInsertPosition(TypeDeclaration typeDecl) {
        FieldDeclaration[] fields = typeDecl.getFields();

        // If no fields exist, insert at position 0
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
                // Check if static (modifier 0x8 = Modifier.STATIC)
                if ((field.getModifiers() & org.eclipse.jdt.core.dom.Modifier.STATIC) != 0) {
                    lastStaticFieldIndex = i;
                }
            }
        }

        // Insert after last static field, or at beginning if no static fields
        return lastStaticFieldIndex + 1;
    }
}
