package ro.gs1.log4e2026.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.jdt.ASTUtil;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Handler for exchanging the logging framework in a class.
 * Converts between SLF4J, Log4j2, and JUL.
 */
public class ExchangeFrameworkHandler extends AbstractHandler {

    // Patterns for detecting frameworks
    private static final Pattern SLF4J_IMPORT = Pattern.compile("org\\.slf4j\\.");
    private static final Pattern LOG4J2_IMPORT = Pattern.compile("org\\.apache\\.logging\\.log4j\\.");
    private static final Pattern JUL_IMPORT = Pattern.compile("java\\.util\\.logging\\.");

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);

        if (!(editor instanceof ITextEditor)) {
            return null;
        }

        ITextEditor textEditor = (ITextEditor) editor;
        ICompilationUnit cu = JavaUI.getWorkingCopyManager()
                .getWorkingCopy(textEditor.getEditorInput());

        if (cu == null) {
            return null;
        }

        try {
            exchangeFramework(cu, textEditor);
        } catch (Exception e) {
            Log4e2026Plugin.logError("Failed to exchange framework", e);
            throw new ExecutionException("Failed to exchange framework", e);
        }

        return null;
    }

    private void exchangeFramework(ICompilationUnit cu, ITextEditor editor) throws Exception {
        // Parse the compilation unit
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        // Detect current framework
        String currentFramework = detectFramework(astRoot);
        if (currentFramework == null) {
            Log4e2026Plugin.logWarning("Could not detect logging framework");
            return;
        }

        // Get target framework from preferences
        String targetFramework = Log4e2026Plugin.getPreferences().getString("loggingFramework");
        if (targetFramework == null || targetFramework.isEmpty()) {
            targetFramework = LoggerTemplates.SLF4J;
        }

        if (currentFramework.equals(targetFramework)) {
            Log4e2026Plugin.log("Already using " + targetFramework + " framework");
            return;
        }

        // Get the target template
        LoggerTemplate targetTemplate = LoggerTemplates.getTemplate(targetFramework);
        if (targetTemplate == null) {
            Log4e2026Plugin.logWarning("Unknown target framework: " + targetFramework);
            return;
        }

        AST ast = astRoot.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        // Find and update logger imports
        updateImports(astRoot, rewrite, ast, currentFramework, targetTemplate);

        // Find and update logger declaration
        updateLoggerDeclaration(astRoot, rewrite, ast, targetTemplate);

        // Apply the rewrite
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true)).apply(document);

        Log4e2026Plugin.log("Framework exchanged from " + currentFramework + " to " + targetFramework);
    }

    private String detectFramework(CompilationUnit cu) {
        for (Object obj : cu.imports()) {
            ImportDeclaration imp = (ImportDeclaration) obj;
            String name = ASTUtil.getName(imp);

            if (SLF4J_IMPORT.matcher(name).find()) {
                return LoggerTemplates.SLF4J;
            }
            if (LOG4J2_IMPORT.matcher(name).find()) {
                return LoggerTemplates.LOG4J2;
            }
            if (JUL_IMPORT.matcher(name).find()) {
                return LoggerTemplates.JUL;
            }
        }
        return null;
    }

    private void updateImports(CompilationUnit cu, ASTRewrite rewrite, AST ast,
                               String oldFramework, LoggerTemplate targetTemplate) {
        // Remove old imports
        for (Object obj : cu.imports()) {
            ImportDeclaration imp = (ImportDeclaration) obj;
            String name = ASTUtil.getName(imp);

            boolean shouldRemove = false;
            switch (oldFramework) {
                case LoggerTemplates.SLF4J:
                    shouldRemove = SLF4J_IMPORT.matcher(name).find();
                    break;
                case LoggerTemplates.LOG4J2:
                    shouldRemove = LOG4J2_IMPORT.matcher(name).find();
                    break;
                case LoggerTemplates.JUL:
                    shouldRemove = JUL_IMPORT.matcher(name).find();
                    break;
            }

            if (shouldRemove) {
                rewrite.remove(imp, null);
            }
        }

        // Add new imports (will be done via ICompilationUnit.createImport later)
    }

    @SuppressWarnings("unchecked")
    private void updateLoggerDeclaration(CompilationUnit cu, ASTRewrite rewrite, AST ast,
                                          LoggerTemplate targetTemplate) {
        // Find the logger field and update it
        if (cu.types().isEmpty()) {
            return;
        }

        TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
        String className = ASTUtil.getName(typeDecl);

        for (FieldDeclaration field : typeDecl.getFields()) {
            for (Object fragment : field.fragments()) {
                VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                String fieldName = ASTUtil.getName(vdf);

                // Check if this looks like a logger field
                if (isLoggerField(fieldName)) {
                    // Create new logger declaration
                    String loggerName = Log4e2026Plugin.getPreferences().getString("loggerName");
                    if (loggerName == null || loggerName.isEmpty()) {
                        loggerName = "logger";
                    }

                    String newDeclaration = targetTemplate.getDeclaration()
                            .replace("${enclosing_type}", className)
                            .replace("${logger}", loggerName);

                    // Parse and replace
                    ASTParser fieldParser = ASTParser.newParser(AST.getJLSLatest());
                    fieldParser.setSource(("class Temp { " + newDeclaration + " }").toCharArray());
                    fieldParser.setKind(ASTParser.K_COMPILATION_UNIT);
                    CompilationUnit tempCu = (CompilationUnit) fieldParser.createAST(null);
                    TypeDeclaration tempType = (TypeDeclaration) tempCu.types().get(0);
                    FieldDeclaration newField = tempType.getFields()[0];

                    FieldDeclaration copiedField = (FieldDeclaration) ASTNode.copySubtree(ast, newField);
                    rewrite.replace(field, copiedField, null);
                    return; // Only replace first logger field
                }
            }
        }
    }

    private boolean isLoggerField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lower = fieldName.toLowerCase();
        return lower.equals("logger") || lower.equals("log") || lower.equals("_logger")
                || lower.equals("_log");
    }
}
