package ro.gs1.log4e2026.handlers;

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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.preferences.PreferenceConstants;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

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

        // Get preferences
        String framework = Log4e2026Plugin.getPreferences()
                .getString(PreferenceConstants.P_LOGGING_FRAMEWORK);
        String loggerName = Log4e2026Plugin.getPreferences()
                .getString(PreferenceConstants.P_LOGGER_NAME);

        // Get the template
        LoggerTemplate template = LoggerTemplates.getTemplate(framework);
        if (template == null) {
            template = LoggerTemplates.getSLF4J();
        }

        // Create AST rewrite
        AST ast = astRoot.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

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

        // Create the logger field declaration
        String className = primaryType.getElementName();
        String declaration = template.getDeclaration()
                .replace("${enclosing_type}", className)
                .replace("${logger}", loggerName);

        // Parse the field declaration
        ASTParser fieldParser = ASTParser.newParser(AST.getJLSLatest());
        fieldParser.setSource(("class Temp { " + declaration + " }").toCharArray());
        fieldParser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit tempCu = (CompilationUnit) fieldParser.createAST(null);
        TypeDeclaration tempType = (TypeDeclaration) tempCu.types().get(0);
        FieldDeclaration tempField = tempType.getFields()[0];

        // Copy the field to the target AST
        FieldDeclaration newField = (FieldDeclaration) ASTNode.copySubtree(ast, tempField);

        // Add the field at the beginning of the type
        ListRewrite listRewrite = rewrite.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertFirst(newField, null);

        // Add import if needed
        String importStatement = template.getImportStatement();
        if (importStatement != null && !importStatement.isEmpty()) {
            cu.createImport(importStatement, null, null);
            // Also import factory class
            if (framework.equals(LoggerTemplates.SLF4J)) {
                cu.createImport("org.slf4j.LoggerFactory", null, null);
            } else if (framework.equals(LoggerTemplates.LOG4J2)) {
                cu.createImport("org.apache.logging.log4j.LogManager", null, null);
            }
        }

        // Apply the rewrite
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        TextEdit edits = rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
        edits.apply(document);

        Log4e2026Plugin.log("Logger declared successfully");
    }
}
