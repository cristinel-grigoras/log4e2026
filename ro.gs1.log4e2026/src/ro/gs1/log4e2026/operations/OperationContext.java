package ro.gs1.log4e2026.operations;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.IDocument;

/**
 * Context object containing all information needed for a logging operation.
 */
public class OperationContext {

    private ICompilationUnit compilationUnit;
    private CompilationUnit astRoot;
    private MethodDeclaration selectedMethod;
    private IDocument document;
    private IProgressMonitor progressMonitor;
    private Map<String, String> compilerOptions;
    private int selectionOffset;
    private int selectionLength;

    public OperationContext() {
    }

    public ICompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void setCompilationUnit(ICompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public CompilationUnit getAstRoot() {
        return astRoot;
    }

    public void setAstRoot(CompilationUnit astRoot) {
        this.astRoot = astRoot;
    }

    public MethodDeclaration getSelectedMethod() {
        return selectedMethod;
    }

    public void setSelectedMethod(MethodDeclaration selectedMethod) {
        this.selectedMethod = selectedMethod;
    }

    public IDocument getDocument() {
        return document;
    }

    public void setDocument(IDocument document) {
        this.document = document;
    }

    public IProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public void setProgressMonitor(IProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    public Map<String, String> getCompilerOptions() {
        return compilerOptions;
    }

    public void setCompilerOptions(Map<String, String> compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    public int getSelectionOffset() {
        return selectionOffset;
    }

    public void setSelectionOffset(int selectionOffset) {
        this.selectionOffset = selectionOffset;
    }

    public int getSelectionLength() {
        return selectionLength;
    }

    public void setSelectionLength(int selectionLength) {
        this.selectionLength = selectionLength;
    }

    public IJavaProject getJavaProject() {
        if (compilationUnit != null) {
            return compilationUnit.getJavaProject();
        }
        return null;
    }

    public boolean isCancelled() {
        return progressMonitor != null && progressMonitor.isCanceled();
    }

    public void checkCancelled() throws InterruptedException {
        if (isCancelled()) {
            throw new InterruptedException("Operation cancelled by user");
        }
    }
}
