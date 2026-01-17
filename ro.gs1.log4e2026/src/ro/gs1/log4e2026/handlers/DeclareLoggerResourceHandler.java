package ro.gs1.log4e2026.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.handlers.HandlerUtil;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.preferences.PreferenceConstants;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Handler for declaring loggers in selected resources from Package Explorer.
 */
public class DeclareLoggerResourceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        List<ICompilationUnit> compilationUnits = collectCompilationUnits(structuredSelection);

        if (compilationUnits.isEmpty()) {
            return null;
        }

        Job job = new Job("Declaring Loggers") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Declaring loggers", compilationUnits.size());
                int processed = 0;
                int added = 0;

                for (ICompilationUnit cu : compilationUnits) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    monitor.subTask(cu.getElementName());
                    try {
                        if (declareLogger(cu)) {
                            added++;
                        }
                        processed++;
                    } catch (Exception e) {
                        Log4e2026Plugin.logError("Failed to declare logger in " + cu.getElementName(), e);
                    }
                    monitor.worked(1);
                }

                Log4e2026Plugin.log("Declared loggers in " + added + " of " + processed + " files");
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return null;
    }

    private List<ICompilationUnit> collectCompilationUnits(IStructuredSelection selection) {
        List<ICompilationUnit> result = new ArrayList<>();

        for (Iterator<?> it = selection.iterator(); it.hasNext();) {
            Object element = it.next();
            try {
                collectFromElement(element, result);
            } catch (Exception e) {
                Log4e2026Plugin.logError("Error collecting compilation units", e);
            }
        }

        return result;
    }

    private void collectFromElement(Object element, List<ICompilationUnit> result) throws Exception {
        if (element instanceof ICompilationUnit) {
            result.add((ICompilationUnit) element);
        } else if (element instanceof IType) {
            ICompilationUnit cu = ((IType) element).getCompilationUnit();
            if (cu != null) {
                result.add(cu);
            }
        } else if (element instanceof IPackageFragment) {
            IPackageFragment pkg = (IPackageFragment) element;
            for (ICompilationUnit cu : pkg.getCompilationUnits()) {
                result.add(cu);
            }
        } else if (element instanceof IPackageFragmentRoot) {
            IPackageFragmentRoot root = (IPackageFragmentRoot) element;
            for (IJavaElement child : root.getChildren()) {
                if (child instanceof IPackageFragment) {
                    collectFromElement(child, result);
                }
            }
        } else if (element instanceof IJavaProject) {
            IJavaProject project = (IJavaProject) element;
            for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
                if (!root.isArchive()) {
                    collectFromElement(root, result);
                }
            }
        } else if (element instanceof IFile) {
            IFile file = (IFile) element;
            if ("java".equals(file.getFileExtension())) {
                IJavaElement javaElement = JavaCore.create(file);
                if (javaElement instanceof ICompilationUnit) {
                    result.add((ICompilationUnit) javaElement);
                }
            }
        } else if (element instanceof IFolder) {
            IFolder folder = (IFolder) element;
            IJavaElement javaElement = JavaCore.create(folder);
            if (javaElement != null) {
                collectFromElement(javaElement, result);
            }
        } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            IJavaElement javaElement = JavaCore.create(project);
            if (javaElement instanceof IJavaProject) {
                collectFromElement(javaElement, result);
            }
        } else if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IResource resource = adaptable.getAdapter(IResource.class);
            if (resource != null) {
                collectFromElement(resource, result);
            }
        }
    }

    private boolean declareLogger(ICompilationUnit cu) throws Exception {
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
            return false;
        }
        TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);

        // Check if logger already exists
        for (FieldDeclaration field : typeDecl.getFields()) {
            for (Object fragment : field.fragments()) {
                VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                if (vdf.getName().getIdentifier().equals(loggerName)) {
                    return false; // Logger already exists
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

        // Add imports
        String importStatement = template.getImportStatement();
        if (importStatement != null && !importStatement.isEmpty()) {
            cu.createImport(importStatement, null, null);
            if (framework.equals(LoggerTemplates.SLF4J)) {
                cu.createImport("org.slf4j.LoggerFactory", null, null);
            } else if (framework.equals(LoggerTemplates.LOG4J2)) {
                cu.createImport("org.apache.logging.log4j.LogManager", null, null);
            }
        }

        // Apply the rewrite
        String source = cu.getSource();
        Document document = new Document(source);
        TextEdit edits = rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
        edits.apply(document);

        // Save the changes
        cu.getBuffer().setContents(document.get());
        cu.save(null, true);

        return true;
    }
}
